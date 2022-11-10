package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public final class CSVAdapter extends DBConnector {

    private PreparedStatement statement;


    public CSVAdapter(@NotNull String sql) throws SQLException {
        super();
        setStatement(sql);
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public void setStatement(@NotNull String sql) throws SQLException {
        this.statement = createPreparedStatement(sql, true);
    }

    public void writeToCSV(@NotNull File file, @NotNull String @NotNull [] columns, @NotNull String @NotNull [] types)
            throws DataNotFoundException {

        if (!file.getName().endsWith(".csv")) {
            throw new DataNotFoundException("Output file must be of type CSV");
        }
        if (statement == null) {
            throw new DataNotFoundException("No statement to execute");
        }
        int len = columns.length;
        if (len == 0) {
            throw new DataNotFoundException("No given columns");
        }
        if (len != types.length) {
            throw new InvalidDataException("Invalid type array length, make sure it is as long as the header array");
        }

        String col, type; Object next;
        StringBuilder headBuilder = new StringBuilder(),
                      rowBuilder  = new StringBuilder();

        try (ResultSet rs = statement.executeQuery();
             FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter buf = new BufferedWriter(osw)) {

            for (String name : columns) {
                headBuilder.append(name).append(',');
            }
            buf.write(toRowString(headBuilder));
            while (rs.next()) {
                for (int i = 0; i < columns.length; i++) {
                    col = columns[i];
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
                        rowBuilder.append(next).append(',');
                    }
                }
                buf.write(toRowString(rowBuilder));
                rowBuilder.delete(0, rowBuilder.length());
            }
        } catch (SQLException | IOException e) {
            throw new DataNotFoundException(e);
        }
    }

    private static String toRowString(@NotNull StringBuilder sb) {
        if (sb.length() == 0) {
            throw new InvalidDataException("StringBuilder must be longer than 0");
        }
        return sb.deleteCharAt(sb.length() - 1).append('\n').toString();
    }

}
