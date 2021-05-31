package generation.jasmin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import generation.jasmin.JasminAssistant;
import generation.ollir.OllirAssistant;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirErrorException;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class BackendStage implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        System.out.println(ollirClass.getImports());
        try {

            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
//            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            //ollirClass.show(); // print to console main information about the input OLLIR


            JasminAssistant jasminAssistant = new JasminAssistant(ollirClass).generate();

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = jasminAssistant.getCode();; // Convert node ...

            try {
                FileWriter myWriter = new FileWriter("./" + ollirClass.getClassName() + ".j");
                myWriter.write(jasminCode);
                myWriter.close();
                System.err.println("Successfully wrote final jasmin to './" + ollirClass.getClassName() + ".j'");
            } catch (IOException e) {
                System.err.println("An error occurred writing final jasmin to './" + ollirClass.getClassName() + ".j'");
                e.printStackTrace();
            }

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Arrays.asList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }

}
