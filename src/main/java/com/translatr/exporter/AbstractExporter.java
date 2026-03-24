package com.translatr.exporter;

public abstract class AbstractExporter implements Exporter {
    @Override
    public String getContentType() { return "application/octet-stream"; }
}
