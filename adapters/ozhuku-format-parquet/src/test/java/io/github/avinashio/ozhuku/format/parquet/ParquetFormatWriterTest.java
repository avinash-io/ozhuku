package io.github.avinashio.ozhuku.format.parquet;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParquetFormatWriterTest {

    @Test
    void shouldRejectWriteBeforeOpen() {

        final ParquetFormatWriter writer =
                new ParquetFormatWriter(ParquetWriterConfiguration.defaults());

        final Record record = new Record(
                0,
                List.of(),
                RecordMetadata.empty());

        assertThrows(IllegalStateException.class, () -> writer.write(record));
    }

    @Test
    void shouldRejectNullRecordAfterOpen() throws Exception {

        final ParquetFormatWriter writer =
                new ParquetFormatWriter(ParquetWriterConfiguration.defaults());

        writer.open(new ByteArrayOutputStream());

        assertThrows(NullPointerException.class, () -> writer.write(null));

        writer.close();
    }

    @Test
    void shouldRejectOpeningWriterTwice() throws Exception {

        final ParquetFormatWriter writer =
                new ParquetFormatWriter(ParquetWriterConfiguration.defaults());

        writer.open(new ByteArrayOutputStream());

        assertThrows(
                IllegalStateException.class,
                () -> writer.open(new ByteArrayOutputStream()));

        writer.close();
    }
}