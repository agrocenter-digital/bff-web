package com.agrocenter.bff.service;

import com.agrocenter.bff.config.MessagingProperties;
import com.agrocenter.bff.dto.messaging.TopicParticionInfo;
import com.agrocenter.bff.dto.messaging.TopicStatusResponse;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TopicAdminService {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final KafkaAdmin kafkaAdmin;
    private final MessagingProperties properties;

    public TopicAdminService(KafkaAdmin kafkaAdmin, MessagingProperties properties) {
        this.kafkaAdmin = kafkaAdmin;
        this.properties = properties;
    }

    public List<TopicStatusResponse> describirTopicos() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            List<TopicStatusResponse> resultado = new ArrayList<>();
            for (String topico : properties.topicos()) {
                resultado.add(describirTopico(adminClient, topico));
            }
            return resultado;
        } catch (RuntimeException exception) {
            String detalle = "No fue posible conectar con el broker Kafka: " + exception.getMessage();
            return properties.topicos().stream()
                    .map(topico -> degradado(topico, detalle))
                    .toList();
        }
    }

    private TopicStatusResponse describirTopico(AdminClient adminClient, String topico) {
        try {
            DescribeTopicsResult describe = adminClient.describeTopics(List.of(topico));
            Map<String, TopicDescription> descripciones =
                    describe.allTopicNames().get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            TopicDescription descripcion = descripciones.get(topico);
            if (descripcion == null) {
                return degradado(topico, "El topico no existe en el broker");
            }

            List<TopicPartition> particiones = descripcion.partitions().stream()
                    .map(particion -> new TopicPartition(topico, particion.partition()))
                    .toList();
            Map<TopicPartition, OffsetSpec> solicitudOffsets = particiones.stream()
                    .collect(Collectors.toMap(Function.identity(), particion -> OffsetSpec.latest()));
            KafkaFuture<Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo>> offsetsFuture =
                    adminClient.listOffsets(solicitudOffsets).all();
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsets =
                    offsetsFuture.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            long mensajesTotales = 0L;
            boolean degradado = false;
            List<TopicParticionInfo> detalleParticiones = new ArrayList<>();
            for (TopicPartitionInfo particion : descripcion.partitions()) {
                TopicPartition clave = new TopicPartition(topico, particion.partition());
                ListOffsetsResult.ListOffsetsResultInfo info = offsets.get(clave);
                Long offsetFinal = info == null ? null : info.offset();
                if (offsetFinal != null) {
                    mensajesTotales += offsetFinal;
                }
                if (particion.leader() == null) {
                    degradado = true;
                }
                detalleParticiones.add(new TopicParticionInfo(
                        particion.partition(),
                        particion.leader() == null ? null : particion.leader().id(),
                        particion.replicas().stream().map(Node::id).toList(),
                        particion.isr().stream().map(Node::id).toList(),
                        offsetFinal
                ));
            }

            int factorReplicacion = descripcion.partitions().isEmpty()
                    ? 0
                    : descripcion.partitions().get(0).replicas().size();

            return new TopicStatusResponse(
                    topico,
                    degradado ? "DEGRADADO" : "ACTIVO",
                    descripcion.partitions().size(),
                    factorReplicacion,
                    mensajesTotales,
                    "Topico operativo",
                    detalleParticiones
            );
        } catch (TimeoutException exception) {
            return degradado(topico, "Timeout al consultar el broker Kafka");
        } catch (ExecutionException exception) {
            Throwable causa = exception.getCause();
            if (causa instanceof UnknownTopicOrPartitionException) {
                return degradado(topico, "El topico no existe en el broker");
            }
            return degradado(topico, "Error consultando el topico: "
                    + (causa == null ? exception.getMessage() : causa.getMessage()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return degradado(topico, "Consulta interrumpida");
        } catch (RuntimeException exception) {
            return degradado(topico, "Error consultando el topico: " + exception.getMessage());
        }
    }

    private TopicStatusResponse degradado(String topico, String detalle) {
        return new TopicStatusResponse(topico, "DEGRADADO", 0, 0, 0L, detalle, List.of());
    }
}
