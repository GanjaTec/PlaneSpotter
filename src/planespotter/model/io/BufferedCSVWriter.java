package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class BufferedCSVWriter extends DBConnector {

    private static boolean debug = false;
    private static final String DBG = "[CSVWriter-DEBUG]: ";

    private PreparedStatement statement;
    private int maxRows;


    public BufferedCSVWriter(@NotNull String sql) throws SQLException {
        this(sql, Integer.MAX_VALUE);
    }

    public BufferedCSVWriter(String sql, int maxRows) throws SQLException {
        super();
        setStatement(sql);
        this.maxRows = maxRows;
    }

    public static boolean inDebugMode() {
        return debug;
    }

    public static void setDebugMode(boolean mode) {
        debug = mode;
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public void setStatement(@NotNull String sql) throws SQLException {
        this.statement = createPreparedStatement(sql, true);
    }

    public void writeToCSV(@NotNull File file, @NotNull String @NotNull [] header, @NotNull String @NotNull [] types)
            throws DataNotFoundException, IOException {

        if (inDebugMode())
            System.out.println(DBG + "in write method");

        if (!file.getName().endsWith(".csv")) {
            throw new IOException(new InvalidDataException("Output file must be of type CSV"));
        }
        if (statement == null) {
            throw new DataNotFoundException("No statement to execute, must be set before");
        }
        int len = header.length;
        if (len == 0) {
            throw new IOException(new InvalidDataException("No given columns"));
        }
        if (len != types.length) {
            throw new IOException(new InvalidDataException("Invalid type array length, make sure it is as long as the header array"));
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new FileNotFoundException("Could not create file '" + file.getAbsolutePath() + "'");
        }

        if (inDebugMode()) {
            System.out.println(DBG + "checks passed");
        }

        String type; Object next; int row, col;

        RowWriter writer = null;
        if (inDebugMode()) {
            System.out.println(DBG + "entering try/catch");
        }
        try (ResultSet rs = statement.executeQuery()) {
            // stream resources
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter buf = new BufferedWriter(osw);
            writer = new RowWriter(buf);

            if (inDebugMode()) {
                System.out.println(DBG + "entering while");
            }
            writer.writeRow(header);
            row = 0;
            while (rs.next()) {
                if (inDebugMode())
                    System.out.println(DBG + "current row: " + row + " (max=" + maxRows + ")");
                if (row++ >= maxRows) {
                    if (inDebugMode())
                        System.out.println(DBG + "max rows reached");
                    return;
                }
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
                        default -> "null";
                    };
                    if (next == null) {
                        next = "NA";
                    }
                    writer.write(next).comma();
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

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
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
