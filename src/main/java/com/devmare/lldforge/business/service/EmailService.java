package com.devmare.lldforge.business.service;

import java.util.Map;

public interface EmailService {
    void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
