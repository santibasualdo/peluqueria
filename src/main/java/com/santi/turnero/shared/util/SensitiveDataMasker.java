package com.santi.turnero.shared.util;

public final class SensitiveDataMasker {

    private SensitiveDataMasker() {
    }

    public static String maskPhone(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }

        String digits = telefono.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "****";
        }

        return "****" + digits.substring(digits.length() - 4);
    }

    public static String summarizeText(String texto) {
        if (texto == null || texto.isBlank()) {
            return "sin_texto";
        }

        return "len=" + texto.trim().length();
    }
}
