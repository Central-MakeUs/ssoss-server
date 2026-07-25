package com.ssoss.ssossbackend.content.infrastructure.ai;

record TagAttribute(String value) {

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
