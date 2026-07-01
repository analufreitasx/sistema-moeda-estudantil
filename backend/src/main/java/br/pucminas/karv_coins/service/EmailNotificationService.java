package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.config.MailProperties;
import br.pucminas.karv_coins.dto.notification.ResgateNotificacaoDto;
import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy '\u00e0s' HH:mm");
    private static final String APP_SLOGAN = "Mérito que vira oportunidade";

    private final MailProperties mailProperties;
    private final JavaMailSender mailSender;

    private record EmailConteudo(String texto, String html) {
    }

    private record DetalheEmail(String rotulo, String valor) {
    }

    private record LinkEmail(String rotulo, String url) {
    }

    public EmailNotificationService(MailProperties mailProperties, JavaMailSender mailSender) {
        this.mailProperties = mailProperties;
        this.mailSender = mailSender;
    }

    public void notificarEnvioMoedas(List<TransacaoNotificacaoDto> transacoes) {
        if (transacoes == null || transacoes.isEmpty()) {
            log.warn("Solicitada notificacao de envio de moedas sem transacoes.");
            return;
        }
        for (TransacaoNotificacaoDto transacao : transacoes) {
            notificarEnvioMoedas(transacao);
        }
    }

    public void notificarEnvioMoedas(TransacaoNotificacaoDto transacao) {
        if (transacao == null) {
            log.warn("Solicitada notificacao de envio de moedas com transacao nula.");
            return;
        }
        notificarRecebimentoAluno(transacao);
        notificarEnvioProfessor(transacao);
    }

    public void notificarRecebimentoAluno(TransacaoNotificacaoDto transacao) {
        String dataFormatada = transacao.data().format(FORMATO_DATA);
        String assunto = "[%s] Voc\u00ea recebeu moedas".formatted(mailProperties.appName());
        String texto = """
                Ol\u00e1, %s!

                Voc\u00ea recebeu %s de %s.

                Motivo: %s
                Data da transa\u00e7\u00e3o: %s
                Seu saldo atual: %s

                Acesse o extrato no KarvCoins para acompanhar suas movimenta\u00e7\u00f5es.
                """.formatted(
                transacao.alunoNome(),
                formatarMoedas(transacao.valor()),
                transacao.professorNome(),
                transacao.motivo(),
                dataFormatada,
                formatarMoedas(transacao.saldoAluno())
        );
        EmailConteudo conteudo = new EmailConteudo(
                texto,
                criarEmailHtml(
                        "Moedas recebidas no KarvCoins",
                        "Você recebeu %s".formatted(formatarMoedas(transacao.valor())),
                        "Olá, %s!".formatted(transacao.alunoNome()),
                        "Seu desempenho acadêmico foi reconhecido. As moedas já foram creditadas no seu saldo KarvCoins.",
                        "Saldo atual",
                        formatarMoedas(transacao.saldoAluno()),
                        List.of(
                                new DetalheEmail("Recebido de", transacao.professorNome()),
                                new DetalheEmail("Motivo", transacao.motivo()),
                                new DetalheEmail("Data da transação", dataFormatada),
                                new DetalheEmail("Moedas recebidas", formatarMoedas(transacao.valor()))
                        ),
                        null,
                        "Abra o KarvCoins e consulte seu extrato para acompanhar todas as movimentações."
                )
        );

        enviar(transacao.alunoEmail(), assunto, conteudo);
    }

    public void notificarEnvioProfessor(TransacaoNotificacaoDto transacao) {
        String dataFormatada = transacao.data().format(FORMATO_DATA);
        String assunto = "[%s] Confirma\u00e7\u00e3o de envio de moedas".formatted(mailProperties.appName());
        String texto = """
                Ol\u00e1, %s!

                Seu envio de %s para %s foi registrado com sucesso.

                Motivo: %s
                Data da transa\u00e7\u00e3o: %s
                Seu saldo atual: %s

                Consulte o extrato no KarvCoins para ver o hist\u00f3rico de distribui\u00e7\u00f5es.
                """.formatted(
                transacao.professorNome(),
                formatarMoedas(transacao.valor()),
                transacao.alunoNome(),
                transacao.motivo(),
                dataFormatada,
                formatarMoedas(transacao.saldoProfessor())
        );
        EmailConteudo conteudo = new EmailConteudo(
                texto,
                criarEmailHtml(
                        "Envio de moedas registrado",
                        "Envio registrado com sucesso",
                        "Olá, %s!".formatted(transacao.professorNome()),
                        "A distribuição de moedas foi concluída e já aparece no histórico do KarvCoins.",
                        "Saldo atual",
                        formatarMoedas(transacao.saldoProfessor()),
                        List.of(
                                new DetalheEmail("Aluno", transacao.alunoNome()),
                                new DetalheEmail("Motivo", transacao.motivo()),
                                new DetalheEmail("Data da transação", dataFormatada),
                                new DetalheEmail("Moedas enviadas", formatarMoedas(transacao.valor()))
                        ),
                        null,
                        "Consulte o extrato para acompanhar o histórico de distribuições."
                )
        );

        enviar(transacao.professorEmail(), assunto, conteudo);
    }

    public void notificarResgateVantagem(ResgateNotificacaoDto resgate, byte[] qrCodePng) {
        if (resgate == null) {
            log.warn("Solicitada notificacao de resgate com dados nulos.");
            return;
        }
        String nomeAnexo = "resgate-karvcoins-%s.png".formatted(resgate.codigoCupom());
        notificarResgateAluno(resgate, qrCodePng, nomeAnexo);
        notificarResgateEmpresa(resgate, qrCodePng, nomeAnexo);
    }

    private void notificarResgateAluno(ResgateNotificacaoDto resgate, byte[] qrCodePng, String nomeAnexo) {
        String dataFormatada = resgate.data().format(FORMATO_DATA);
        String assunto = "[%s] Resgate de vantagem confirmado".formatted(mailProperties.appName());
        String texto = """
                Olá, %s!

                Seu resgate da vantagem \"%s\" foi registrado com sucesso.

                Empresa parceira: %s
                Valor do resgate: %s
                Código do cupom: %s
                Data do resgate: %s
                Seu saldo atual: %s

                Link de verificação: %s

                Apresente o QRCode anexado ou o código do cupom no atendimento presencial.
                """.formatted(
                resgate.alunoNome(),
                resgate.vantagemDescricao(),
                resgate.empresaNome(),
                formatarMoedas(resgate.valorMoedas()),
                resgate.codigoCupom(),
                dataFormatada,
                formatarMoedas(resgate.saldoAluno()),
                resgate.urlVerificacao()
        );
        EmailConteudo conteudo = new EmailConteudo(
                texto,
                criarEmailHtml(
                        "Resgate de vantagem confirmado",
                        "Resgate confirmado",
                        "Olá, %s!".formatted(resgate.alunoNome()),
                        "Sua vantagem foi reservada com sucesso. Use o código abaixo ou o QR Code anexado no atendimento presencial.",
                        "Código do cupom",
                        resgate.codigoCupom(),
                        List.of(
                                new DetalheEmail("Vantagem", resgate.vantagemDescricao()),
                                new DetalheEmail("Empresa parceira", resgate.empresaNome()),
                                new DetalheEmail("Valor do resgate", formatarMoedas(resgate.valorMoedas())),
                                new DetalheEmail("Data do resgate", dataFormatada),
                                new DetalheEmail("Saldo atual", formatarMoedas(resgate.saldoAluno()))
                        ),
                        new LinkEmail("Verificar resgate", resgate.urlVerificacao()),
                        "O QR Code deste resgate está anexado a este e-mail."
                )
        );

        enviarComAnexo(resgate.alunoEmail(), assunto, conteudo, nomeAnexo, qrCodePng);
    }

    private void notificarResgateEmpresa(ResgateNotificacaoDto resgate, byte[] qrCodePng, String nomeAnexo) {
        String dataFormatada = resgate.data().format(FORMATO_DATA);
        String assunto = "[%s] Novo resgate de vantagem".formatted(mailProperties.appName());
        String texto = """
                Olá, %s!

                Um aluno resgatou uma vantagem oferecida pela sua empresa.

                Aluno: %s
                E-mail do aluno: %s
                Vantagem: %s
                Valor do resgate: %s
                Código do cupom: %s
                Data do resgate: %s

                Link de verificação: %s

                Use o link ou o QRCode anexado para conferir o resgate durante o atendimento presencial.
                """.formatted(
                resgate.empresaNome(),
                resgate.alunoNome(),
                resgate.alunoEmail(),
                resgate.vantagemDescricao(),
                formatarMoedas(resgate.valorMoedas()),
                resgate.codigoCupom(),
                dataFormatada,
                resgate.urlVerificacao()
        );
        EmailConteudo conteudo = new EmailConteudo(
                texto,
                criarEmailHtml(
                        "Novo resgate de vantagem",
                        "Novo resgate recebido",
                        "Olá, %s!".formatted(resgate.empresaNome()),
                        "Um aluno resgatou uma vantagem oferecida pela sua empresa. Confira os dados para validar o atendimento.",
                        "Código do cupom",
                        resgate.codigoCupom(),
                        List.of(
                                new DetalheEmail("Aluno", resgate.alunoNome()),
                                new DetalheEmail("E-mail do aluno", resgate.alunoEmail()),
                                new DetalheEmail("Vantagem", resgate.vantagemDescricao()),
                                new DetalheEmail("Valor do resgate", formatarMoedas(resgate.valorMoedas())),
                                new DetalheEmail("Data do resgate", dataFormatada)
                        ),
                        new LinkEmail("Abrir verificação", resgate.urlVerificacao()),
                        "Use o link ou o QR Code anexado para conferir o resgate durante o atendimento presencial."
                )
        );

        enviarComAnexo(resgate.empresaEmail(), assunto, conteudo, nomeAnexo, qrCodePng);
    }

    private String criarEmailHtml(
            String preHeader,
            String titulo,
            String saudacao,
            String mensagem,
            String destaqueRotulo,
            String destaqueValor,
            List<DetalheEmail> detalhes,
            LinkEmail link,
            String nota
    ) {
        String detalhesHtml = montarDetalhesHtml(detalhes);
        String linkHtml = montarLinkHtml(link);
        String notaHtml = montarNotaHtml(nota);

        return """
                <!doctype html>
                <html lang="pt-BR">
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                  </head>
                  <body style="margin:0; padding:0; background-color:#031017; color:#f8fafc;">
                    <span style="display:none; max-height:0; overflow:hidden; opacity:0; color:transparent;">%s</span>
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="width:100%%; border-collapse:collapse; background-color:#031017; background-image:radial-gradient(circle at top left, rgba(0,131,162,0.24), transparent 460px), radial-gradient(circle at top right, rgba(85,212,230,0.12), transparent 380px);">
                      <tr>
                        <td align="center" style="padding:32px 16px;">
                          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="width:100%%; max-width:640px; border-collapse:separate; border-spacing:0; font-family:Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
                            <tr>
                              <td style="padding:28px 28px 18px; border:1px solid rgba(85,212,230,0.18); border-bottom:0; border-radius:32px 32px 0 0; background-color:#0b2431;">
                                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="width:100%%; border-collapse:collapse;">
                                  <tr>
                                    <td style="width:64px; vertical-align:middle;">
                                      <div style="width:56px; height:56px; border-radius:22px; border:1px solid rgba(85,212,230,0.25); background-color:rgba(29,183,208,0.14); color:#c9f4fb; font-family:Georgia, 'Times New Roman', serif; font-size:20px; font-weight:700; line-height:56px; text-align:center;">KC</div>
                                    </td>
                                    <td style="vertical-align:middle;">
                                      <div style="font-family:Georgia, 'Times New Roman', serif; font-size:22px; font-weight:700; color:#ffffff; letter-spacing:-0.02em;">%s</div>
                                      <div style="margin-top:4px; color:#c9f4fb; font-size:12px; letter-spacing:0.08em; text-transform:uppercase;">%s</div>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:0 28px 28px; border:1px solid rgba(85,212,230,0.18); border-top:0; border-radius:0 0 32px 32px; background-color:#0b2431; box-shadow:0 24px 70px rgba(0,0,0,0.32);">
                                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="width:100%%; border-collapse:collapse;">
                                  <tr>
                                    <td style="padding:30px 26px; border-radius:26px; border:1px solid rgba(85,212,230,0.15); border-top:3px solid #0083a2; background-color:#061821;">
                                      <div style="color:#55d4e6; font-size:12px; font-weight:700; letter-spacing:0.16em; text-transform:uppercase;">KarvCoins</div>
                                      <h1 style="margin:12px 0 0; color:#ffffff; font-family:Georgia, 'Times New Roman', serif; font-size:32px; line-height:1.2; letter-spacing:-0.03em;">%s</h1>
                                      <p style="margin:18px 0 0; color:#f8fafc; font-size:17px; font-weight:700; line-height:1.5;">%s</p>
                                      <p style="margin:10px 0 0; color:#cbd5e1; font-size:15px; line-height:1.7;">%s</p>
                                    </td>
                                  </tr>
                                  <tr>
                                    <td style="padding:22px 0 0;">
                                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="width:100%%; border-collapse:collapse; border-radius:24px; background-color:rgba(0,131,162,0.12); border:1px solid rgba(85,212,230,0.2);">
                                        <tr>
                                          <td style="padding:20px 24px;">
                                            <span style="display:block; color:#96e8f4; font-size:12px; font-weight:700; letter-spacing:0.16em; text-transform:uppercase;">%s</span>
                                            <span style="display:block; margin-top:6px; color:#ffffff; font-size:30px; font-weight:800; letter-spacing:-0.03em;">%s</span>
                                          </td>
                                        </tr>
                                      </table>
                                    </td>
                                  </tr>
                                  <tr>
                                    <td style="padding:20px 0 0;">
                                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="width:100%%; border-collapse:collapse;">
                                        %s
                                      </table>
                                    </td>
                                  </tr>
                                  %s
                                  %s
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td align="center" style="padding:18px 12px 0; color:#94a3b8; font-size:12px; line-height:1.6;">
                                Este e-mail foi enviado automaticamente pelo %s.
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(
                escapeHtml(titulo),
                escapeHtml(preHeader),
                escapeHtml(mailProperties.appName()),
                escapeHtml(APP_SLOGAN),
                escapeHtml(titulo),
                escapeHtml(saudacao),
                escapeHtml(mensagem),
                escapeHtml(destaqueRotulo),
                escapeHtml(destaqueValor),
                detalhesHtml,
                linkHtml,
                notaHtml,
                escapeHtml(mailProperties.appName())
        );
    }

    private static String montarDetalhesHtml(List<DetalheEmail> detalhes) {
        StringBuilder html = new StringBuilder();
        for (DetalheEmail detalhe : detalhes) {
            html.append("""
                    <tr>
                      <td style="padding:14px 0; border-bottom:1px solid rgba(85,212,230,0.12);">
                        <span style="display:block; color:#94a3b8; font-size:12px; font-weight:700; letter-spacing:0.14em; text-transform:uppercase;">%s</span>
                        <span style="display:block; margin-top:5px; color:#f8fafc; font-size:15px; font-weight:700; line-height:1.5;">%s</span>
                      </td>
                    </tr>
                    """.formatted(escapeHtml(detalhe.rotulo()), escapeHtml(detalhe.valor())));
        }
        return html.toString();
    }

    private static String montarLinkHtml(LinkEmail link) {
        if (link == null || link.url() == null || link.url().isBlank()) {
            return "";
        }
        return """
                <tr>
                  <td align="center" style="padding:24px 0 0;">
                    <a href="%s" style="display:inline-block; padding:14px 22px; border-radius:999px; background-color:#0083a2; color:#ffffff; text-decoration:none; font-size:14px; font-weight:800; box-shadow:0 16px 30px rgba(0,131,162,0.22);">%s</a>
                  </td>
                </tr>
                """.formatted(escapeHtml(link.url()), escapeHtml(link.rotulo()));
    }

    private static String montarNotaHtml(String nota) {
        if (nota == null || nota.isBlank()) {
            return "";
        }
        return """
                <tr>
                  <td style="padding:22px 0 0;">
                    <div style="border-radius:20px; border:1px solid rgba(85,212,230,0.14); background-color:rgba(18,50,67,0.72); padding:16px 18px; color:#cbd5e1; font-size:14px; line-height:1.7;">%s</div>
                  </td>
                </tr>
                """.formatted(escapeHtml(nota));
    }

    private static String formatarMoedas(double valor) {
        String quantidade = "%.0f".formatted(valor);
        return "%s %s".formatted(quantidade, "1".equals(quantidade) ? "moeda" : "moedas");
    }

    private static String escapeHtml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void enviar(String destinatario, String assunto, EmailConteudo conteudo) {
        if (!mailProperties.enabled()) {
            log.info(
                    "E-mail desabilitado (app.mail.enabled=false). Destinat\u00e1rio: {}, assunto: {}",
                    destinatario,
                    assunto
            );
            return;
        }
        if (mailProperties.from() == null || mailProperties.from().isBlank()) {
            log.error("E-mail habilitado, mas app.mail.from n\u00e3o foi configurado. Envio para {} cancelado.", destinatario);
            return;
        }

        try {
            var mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudo.texto(), conteudo.html());
            mailSender.send(mensagem);
            log.info("E-mail enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", destinatario, e.getMessage(), e);
        }
    }

    private void enviarComAnexo(
            String destinatario,
            String assunto,
            EmailConteudo conteudo,
            String nomeAnexo,
            byte[] anexo
    ) {
        if (!mailProperties.enabled()) {
            log.info(
                    "E-mail desabilitado (app.mail.enabled=false). Destinatário: {}, assunto: {}",
                    destinatario,
                    assunto
            );
            return;
        }
        if (mailProperties.from() == null || mailProperties.from().isBlank()) {
            log.error("E-mail habilitado, mas app.mail.from não foi configurado. Envio para {} cancelado.", destinatario);
            return;
        }

        try {
            var mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudo.texto(), conteudo.html());
            helper.addAttachment(nomeAnexo, new ByteArrayResource(anexo), "image/png");
            mailSender.send(mensagem);
            log.info("E-mail com anexo enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail com anexo para {}: {}", destinatario, e.getMessage(), e);
        }
    }
}
