package com.progressoft.tools;


import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class NormalizerTool implements Normalizer {
    static String filePathStr = null;
    static String fileDestPath = null;

    static List<List<String>> dataFrame = new ArrayList<>();
    static List<String> dataFrameHeader = new ArrayList<>();
    static Integer selectedColIdx = null;

    public static List<BigDecimal> readCSV(String path, String colToStandardize) {

        dataFrame.clear();

        String line;

        List<BigDecimal> column = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            dataFrameHeader = Arrays.asList(br.readLine().split(","));
            if (dataFrameHeader.stream().noneMatch(colToStandardize::equals))
                throw new IllegalArgumentException("column " + colToStandardize + " not found");
            selectedColIdx = dataFrameHeader.lastIndexOf(colToStandardize);

            while ((line = br.readLine()) != null) {
                String[] attributes = line.split(",");
                dataFrame.add(new ArrayList(Arrays.asList(attributes)));
                column.add(new BigDecimal(attributes[selectedColIdx]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return column;
    }

    static public void writeCVS(String destPath, List<BigDecimal> result, String colToStandardize, String postFix) {
        File file = new File(destPath);
        try (PrintWriter pw = new PrintWriter(file)) {

            dataFrameHeader = new ArrayList(dataFrameHeader);
            dataFrameHeader.add(selectedColIdx + 1, colToStandardize + postFix);
            pw.println(String.join(",", dataFrameHeader));

            if (dataFrame.size() == result.size())
                for (List<String> item : dataFrame) {
                    item.add(selectedColIdx + 1, String.valueOf(result.get(dataFrame.indexOf(item))));
                    pw.println(String.join(",", item));
                }
            else
                throw new IllegalArgumentException("Size of data corrupted");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<BigDecimal> getColByFileType(Path path, Path destPath, String colToStandardize) throws FileNotFoundException {
        if (destPath == null)
            throw new IllegalArgumentException("source file not found");

        filePathStr = path.toString();
        fileDestPath = destPath.toString();

        String fileType = filePathStr.substring(filePathStr.lastIndexOf('.') + 1);

        List<BigDecimal> returnedCol = null;
        switch (fileType) {
            case "csv":
            case "CSV":
                returnedCol = readCSV(filePathStr, colToStandardize);
                break;
            case "xml":
            case "XML":
                returnedCol = readCSV(fileDestPath, colToStandardize);
                break;
            default:
                break;
        }

        return returnedCol;
    }


    @Override
    public ScoringSummary zscore(Path path, Path destPath, String colToStandardize) throws FileNotFoundException {
        // Define Stander Variable for Z-Score Standardize Process
        List<BigDecimal> returnedCol = getColByFileType(path, destPath, colToStandardize);
        List<BigDecimal> zScoreResult = new ArrayList<>();
        ScoringSummaryTool scoringSummary = new ScoringSummaryTool(returnedCol);

        BigDecimal mean = scoringSummary.mean();
        BigDecimal SD = scoringSummary.standardDeviation();

        for (BigDecimal item : Objects.requireNonNull(returnedCol)) {
            BigDecimal val = item.subtract(mean);
            val = val.divide(SD, 2, RoundingMode.HALF_EVEN);
            zScoreResult.add(val);
        }
        writeCVS(fileDestPath, zScoreResult, colToStandardize, "_z");
        return scoringSummary;
    }

    @Override
    public ScoringSummary minMaxScaling(Path path, Path destPath, String colToNormalize) throws FileNotFoundException {
        if (destPath == null)
            throw new IllegalArgumentException("source file not found");
        // Define Stander Variable for Min Max Scaling Process
        List<BigDecimal> returnedCol = getColByFileType(path, destPath, colToNormalize);
        List<BigDecimal> minMaxList = new ArrayList<>();
        ScoringSummaryTool scoringSummary = new ScoringSummaryTool(returnedCol);

        // return max and min to calculate minMaxScale for each value
        BigDecimal max = scoringSummary.max(), min = scoringSummary.min();

        for (BigDecimal x : returnedCol) {
            BigDecimal xSub = x.subtract(min);
            BigDecimal mSubM = max.subtract(min);
            BigDecimal mm = xSub.divide(mSubM, 2, RoundingMode.HALF_EVEN);
            minMaxList.add(mm);
        }

        writeCVS(fileDestPath, minMaxList, colToNormalize, "_mm");
        return scoringSummary;
    }

    public void callMethod(String flag, Path path, Path destPath, String colToNormalize) throws FileNotFoundException {
        if (flag == null)
            System.out.println("Sorry we don't support this type of Measure, look forward to add it in future.");
        assert flag != null;

        if (flag.equals("min-max")) {
            zscore(path, destPath, colToNormalize);
            System.out.println("Min Max Scaling done you can find the file in dir " + destPath);
        }
        if (flag.equals("z-score")) {
            minMaxScaling(path, destPath, colToNormalize);
            System.out.println("Z-Score done you can find the file in dir " + destPath);
        }

    }


    public static void main(String[] args) throws Exception {
        NormalizerTool tool = new NormalizerTool();

        Path path = FileSystems.getDefault().getPath(args[0]);
        Path destPath = FileSystems.getDefault().getPath(args[1]);
        String colToNormalize = args[2];

        tool.callMethod(args[3], path, destPath, colToNormalize);
    }
}
