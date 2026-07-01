package br.pucminas.karv_coins.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.pucminas.karv_coins.config.MailProperties;
import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

class EmailNotificationServiceTest {

    @Test
    void naoEnviaQuandoEmailEstaDesabilitado() {
        FakeJavaMailSender mailSender = new FakeJavaMailSender();
        EmailNotificationService service = new EmailNotificationService(
                new MailProperties(false, true, "noreply@karvcoins.local", "KarvCoins"),
                mailSender
        );

        service.notificarRecebimentoAluno(transacao());

        assertEquals(0, mailSender.mensagensMimeEnviadas.size());
    }

    @Test
    void enviaMensagemHtmlQuandoEmailEstaHabilitado() throws Exception {
        FakeJavaMailSender mailSender = new FakeJavaMailSender();
        EmailNotificationService service = new EmailNotificationService(
                new MailProperties(true, true, "noreply@karvcoins.local", "KarvCoins"),
                mailSender
        );

        service.notificarRecebimentoAluno(transacao());

        assertEquals(1, mailSender.mensagensMimeEnviadas.size());
        MimeMessage mensagem = mailSender.mensagensMimeEnviadas.getFirst();
        assertEquals("noreply@karvcoins.local", mensagem.getFrom()[0].toString());
        assertEquals("aluno@moeda.local", mensagem.getAllRecipients()[0].toString());
        assertEquals("[KarvCoins] Voc\u00ea recebeu moedas", mensagem.getSubject());

        String conteudo = extrairConteudo(mensagem.getContent());
        assertTrue(conteudo.contains("background-color:#031017"));
        assertTrue(conteudo.contains("#0083a2"));
        assertTrue(conteudo.contains("Mérito que vira oportunidade"));
        assertTrue(conteudo.contains("Você recebeu 10 moedas"));
        assertTrue(conteudo.contains("Seu saldo atual: 120 moedas"));
    }

    private static TransacaoNotificacaoDto transacao() {
        return new TransacaoNotificacaoDto(
                "aluno@moeda.local",
                "Aluno",
                120.0,
                "professor@moeda.local",
                "Professor",
                80.0,
                10.0,
                "Participacao",
                LocalDateTime.of(2026, 5, 24, 17, 0)
        );
    }

    private static String extrairConteudo(Object conteudo) throws IOException, jakarta.mail.MessagingException {
        if (conteudo instanceof String texto) {
            return texto;
        }
        if (conteudo instanceof Multipart multipart) {
            StringBuilder texto = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                texto.append(extrairConteudo(multipart.getBodyPart(i).getContent()));
            }
            return texto.toString();
        }
        return "";
    }

    private static class FakeJavaMailSender implements JavaMailSender {
        private final List<MimeMessage> mensagensMimeEnviadas = new ArrayList<>();

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage((Session) null);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            return createMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            mensagensMimeEnviadas.add(mimeMessage);
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            for (MimeMessage mimeMessage : mimeMessages) {
                send(mimeMessage);
            }
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) {
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) {
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            for (SimpleMailMessage simpleMessage : simpleMessages) {
                send(simpleMessage);
            }
        }
    }
}
