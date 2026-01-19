package com.ajustadoati.sc.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AsistenteFinanciero asistente;

    @PostMapping("/preguntar")
    public String preguntar(@RequestBody String pregunta) {
        return asistente.responder(pregunta);
    }
}
