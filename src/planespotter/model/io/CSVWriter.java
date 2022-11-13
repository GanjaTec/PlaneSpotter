package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class CSVWriter extends DBConnector {

    private final StringBuilder rowBuffer = new StringBuilder();
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
            throws DataNotFoundException {

        if (!file.getName().endsWith(".csv")) {
            throw new DataNotFoundException("Output file must be of type CSV");
        }
        if (statement == null) {
            throw new DataNotFoundException("No statement to execute, must be set before");
        }
        int len = header.length;
        if (len == 0) {
            throw new DataNotFoundException("No given columns");
        }
        if (len != types.length) {
            throw new InvalidDataException("Invalid type array length, make sure it is as long as the header array");
        }

        String type; Object next; int col;

        try (ResultSet rs = statement.executeQuery();
             FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter buf = new BufferedWriter(osw)) {

            for (String name : header) {
                append(name);
            }
            buf.write(nextRow());
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
                        append(next);
                    }
                }
                buf.write(nextRow());
            }
        } catch (SQLException | IOException e) {
            throw new DataNotFoundException(e);
        }
    }

    private void reset() {
        if (rowBuffer.length() > 0) {
            rowBuffer.delete(0, rowBuffer.length());
        }
    }

    private void append(@NotNull Object next) {
        rowBuffer.append(next).append(',');
    }

    private String nextRow() {
        int len = rowBuffer.length();
        if (len == 0) {
            throw new InvalidDataException("StringBuilder must be longer than 0");
        }
        int begin = len - 1;
        String row = rowBuffer.replace(begin, len, "\n").toString();
        reset();
        return row;
    }

}
