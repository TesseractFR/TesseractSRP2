package onl.tesseract.srp.config;

import onl.tesseract.lib.exception.ConfigurationException;
import onl.tesseract.srp.domain.job.EnumJob;
import onl.tesseract.srp.domain.job.JobSkill;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class JobSkillMenuConfigParser {

    /**
     * @throws ConfigurationException If config file not found
     * @throws IOException On IO error
     */
    public JobSkillMenuConfig parseForJob(EnumJob enumJob) throws ConfigurationException, IOException {
        String resourceName = enumJob.name().toLowerCase() + ".skills";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new ConfigurationException("Configuration file not found for skill menu of job " + enumJob);
            }
            String str = new String(stream.readAllBytes());
            List<String> lines = Arrays.asList(str.split("\n"));
            lines = lines.stream().map(l -> l.replace("\r", "")).toList();
            Map<Character, JobSkillMenuConfig.CellType> symbolDefs = parseSymbolDefs(lines);
            List<String> graphLines = lines.stream()
                .dropWhile(l -> !l.startsWith("="))
                .skip(1)
                .takeWhile(l -> !l.equals("END"))
                .toList();
            Collections.reverse(graphLines);
            return parseGraph(graphLines, symbolDefs);
        }
    }

    private JobSkillMenuConfig parseGraph(List<String> lines, Map<Character, JobSkillMenuConfig.CellType> symbolDefs) {
        JobSkillMenuConfig.CellType[][] matrix = new JobSkillMenuConfig.CellType[lines.size()][9];
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            for (int colIndex = 0; colIndex < 9; colIndex++) {
                if (colIndex >= line.length()) {
                    matrix[lineIndex][colIndex] = JobSkillMenuConfig.EmptyCell.INSTANCE;
                } else {
                    char c = line.charAt(colIndex);
                    JobSkillMenuConfig.CellType cellType = symbolDefs.get(c);
                    if (cellType == null) {
                        throw new ConfigurationException("Invalid character '" + c + "'");
                    }
                    matrix[lineIndex][colIndex] = cellType;
                }
            }
        }
        return new JobSkillMenuConfig(matrix);
    }

    private Map<Character, JobSkillMenuConfig.CellType> parseSymbolDefs(List<String> lines) {
        Map<Character, JobSkillMenuConfig.CellType> map = generateDefaultSymbols();
        lines.stream()
            .takeWhile(l -> !l.startsWith("="))
            .forEach(line -> {
                char symbol = line.charAt(0);
                String def = line.split("=")[1];
                if (def.equalsIgnoreCase("ROOT")) {
                    map.put(symbol, JobSkillMenuConfig.RootCell.INSTANCE);
                } else {
                    map.put(symbol, new JobSkillMenuConfig.SkillCell(JobSkill.valueOf(def)));
                }
            });
        return map;
    }

    private Map<Character, JobSkillMenuConfig.CellType> generateDefaultSymbols() {
        Map<Character, JobSkillMenuConfig.CellType> map = new HashMap<>();
        map.put('•', JobSkillMenuConfig.EmptyCell.INSTANCE);
        map.put(' ', JobSkillMenuConfig.EmptyCell.INSTANCE);
        map.put('└', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.TopRight));
        map.put('┘', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.TopLeft));
        map.put('─', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.Horizontal));
        map.put('│', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.Vertical));
        map.put('┬', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.T));
        map.put('┼', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.Cross));
        map.put('┴', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.ReversedT));
        map.put('├', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.RightT));
        map.put('┤', new JobSkillMenuConfig.Arrow(JobSkillMenuConfig.ArrowType.LeftT));
        return map;
    }
}

