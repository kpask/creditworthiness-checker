package com.example.monitoringservice.service;

import com.example.monitoringservice.client.FlaggedRequest;
import com.example.monitoringservice.config.MonitoringProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class ReportMailer {
    private final JavaMailSender mailSender;
    private final MonitoringProperties props;

    public ReportMailer(JavaMailSender mailSender, MonitoringProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    public void send(List<FlaggedRequest> requests) {
        StringBuilder body = new StringBuilder();
        appendSection(body, "Voluntary credit ban active", requests,
                r -> "CHECKED".equals(r.banCheckStatus()));
        appendSection(body, "Ban status unverified (PCR check failed)", requests,
                r -> "FAILED".equals(r.banCheckStatus()));

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("monitoring@pcr.local");
        msg.setTo(props.mail().to());
        msg.setSubject("PCR monitoring: " + requests.size() + " new flagged financing request(s)");
        msg.setText(body.toString());
        mailSender.send(msg);
    }

    private void appendSection(StringBuilder sb, String title, List<FlaggedRequest> all, Predicate<FlaggedRequest> filter) {
        List<FlaggedRequest> items = all.stream().filter(filter).toList();
        if (items.isEmpty()) return;
        sb.append(title).append(" (").append(items.size()).append(")\n");
        items.forEach(r -> sb.append("  - ").append(r.submittedAt())
                .append("  ").append(r.personIdCode())
                .append("  ").append(r.loanType()).append('\n'));
        sb.append('\n');
    }
}