package evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvWriter {

    private final PrintWriter printWriter;

    private final List<String> headers;

    public CsvWriter(String fileName, String... headers) throws IOException {
        this.headers = Arrays.asList(headers);
        FileWriter fileWriter = new FileWriter(fileName);
        printWriter = new PrintWriter(fileWriter);
        String asd = String.join(",", headers);
        printWriter.println(asd);
    }

    public CsvWriter(String fileName, List<String> headers, String... headers2) throws IOException {
        this.headers = headers;
        this.headers.addAll(Arrays.asList(headers2));
        FileWriter fileWriter = new FileWriter(fileName);
        printWriter = new PrintWriter(fileWriter);
        String asd = String.join(",", headers);
        printWriter.println(asd);
    }

    public void write(double... values) {
        String asd = Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
        printWriter.println(asd);
    }

    public void write(Map<String, Object> arguments, double... values) {
        String asd = headers.stream().map(d -> arguments.get(d)).filter(Objects::nonNull).map(d -> String.valueOf(d))
                .collect(Collectors.joining(","));
        printWriter.print(asd);
        printWriter.print(",");
        String asd2 = Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
        printWriter.println(asd2);
    }

    public void close() {
        printWriter.close();
    }
}
