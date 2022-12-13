package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.ExtensionException;
import planespotter.throwables.InvalidDataException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class CSVWriter extends DBConnector {

    private PreparedStatement statement;


    public CSVWriter(@NotNull String sql) throws SQLException {
        super();
        setStatement(sql);
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public void setStatement(@NotNull String sql) throws SQLException {
        this.statement = createPreparedStatement(sql, true);
    }

    public void writeToCSV(@NotNull File file, @NotNull String @NotNull [] header, @NotNull String @NotNull [] types)
            throws DataNotFoundException, IOException {

        if (!file.getName().endsWith(".csv")) {
            throw new InvalidDataException("Output file must be of type CSV");
        }
        if (statement == null) {
            throw new DataNotFoundException("No statement to execute, must be set before");
        }
        int len = header.length;
        if (len == 0) {
            throw new InvalidDataException("No given columns");
        }
        if (len != types.length) {
            throw new InvalidDataException("Invalid type array length, make sure it is as long as the header array");
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new FileNotFoundException("Could not create file '" + file.getAbsolutePath() + "'");
        }

        String type; Object next; int col;

        RowWriter writer = null;
        try (ResultSet rs = statement.executeQuery()) {
            // stream resources
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter buf = new BufferedWriter(osw);
            writer = new RowWriter(buf);

            writer.writeRow(header);
            while (rs.next()) {
                for (int i = 0; i < header.length; i++) {
                    col = i + 1;
                    type = types[i];
                    next = switch (type) {
                        case "int" -> rs.getInt(col);
                        case "long" -> rs.getLong(col);
                        case "float" -> rs.getFloat(col);
                        case "double" -> rs.getDouble(col);
                        case "byte" -> rs.getByte(col);
                        case "short" -> rs.getShort(col);
                        case "string", "String" -> rs.getString(col);
                        default -> null;
                    };
                    if (next != null) {
                        writer.write(next).comma();
                    }
                }
                writer.newLine();
            }
        } catch (SQLException sql) {
            throw new DataNotFoundException(sql);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }


    /**
     *
     */
    private static class RowWriter implements AutoCloseable {

        static final int MAX_LENGTH = 65000;

        // TODO try primitive array
        StringBuilder buffer;
        final BufferedWriter writer;

        RowWriter(BufferedWriter writer) {
            this.buffer = new StringBuilder();
            this.writer = writer;
        }

        int size() {
            return buffer.length();
        }

        RowWriter write(Object o) throws IOException {
            return write(o.toString());
        }


        RowWriter write(char c) throws IOException {
            int len = buffer.length();
            if (size() + len > MAX_LENGTH) {
                write0();
                clearBuffer();
            }
            buffer.append(c);
            return this;
        }

        @SuppressWarnings("all")
        RowWriter comma() throws IOException {
            return write(',');
        }

        RowWriter newLine() {
            int len = size(),
                begin = len - 1;
            buffer.replace(begin, len, "\n");
            return this;
        }

        @SuppressWarnings("all")
        RowWriter writeRow(String... row) throws IOException {
            for (String s : row) {
                write(s).comma();
            }
            return newLine();
        }

        RowWriter write(String str) throws IOException {
            int len = str.length();
            if (size() + len > MAX_LENGTH) {
                try {
                    write0();
                } finally {
                    clearBuffer();
                }
            }
            buffer.append(str);
            return this;
        }

        void write0() throws IOException {
            writer.write(buffer.toString());
        }

        void clearBuffer() {
            buffer = new StringBuilder();
        }

        @Override
        public void close() throws IOException {
            if (size() > 0) {
                try {
                    write0();
                } finally {
                    writer.close();
                }
            }
        }
    }
}
