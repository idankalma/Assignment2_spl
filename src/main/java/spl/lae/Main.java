package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      // TODO: main
        if (args.length != 3) {
            System.out.println("Usage: <num_threads> <input_file> <outputFile>");
            return;
        }

        int numThreads;
        String inputPath = args[1];
        String outputPath = args [2];

        try{
            numThreads = Integer.parseInt((args[0]));

            if(numThreads <= 0){
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e){
            OutputWriter.write("Invalid number of threads", outputPath);
            return;
        }

        try{
            InputParser parser = new InputParser();
            ComputationNode root = parser.parse(inputPath);

            LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);

            ComputationNode output = engine.run(root);

            OutputWriter.write(output.getMatrix(), outputPath);
        }
        catch (ParseException e){ // if the input is not valid
            OutputWriter.write(e.getMessage(), outputPath);
        }
        catch (IllegalArgumentException | IllegalStateException e){ // if the calculation is not valid
            OutputWriter.write(e.getMessage(), outputPath);
        }

    }
}