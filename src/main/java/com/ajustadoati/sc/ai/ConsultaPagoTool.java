package com.ajustadoati.sc.ai;

import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.domain.Pago;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsultaPagoTool {

    private final PagoRepository repo;

    @Tool("""
            Obtiene los pagos de un socio por cédula en una fecha.
            Solo debe usarse cuando el usuario es ADMIN.
            """)
    public List<Pago> consultarPagos(
            String socioId,
            String fecha
    ) {
        LocalDate fechaParseada = normalizarFecha(fecha);
        return repo.findByFechaAndCedula(fechaParseada, socioId);
    }

    private LocalDate normalizarFecha(String input) {

        if (input == null) {
            throw new IllegalArgumentException("Fecha requerida");
        }

        input = input.trim();

        // Caso: viene como objeto serializado
        if (input.startsWith("{")) {
            // ejemplo: {"fecha":"2025-12-26"}
            input = input
                    .replaceAll("[^0-9-]", "")
                    .trim();
        }

        return switch (input.toLowerCase()) {
            case "hoy" -> LocalDate.now();
            case "ayer" -> LocalDate.now().minusDays(1);
            default -> LocalDate.parse(input); // yyyy-MM-dd
        };
    }
}
