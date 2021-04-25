
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.io.StringReader;

public class SyntacticPhase implements JmmParser {

	public JmmParserResult parse(String jmmCode) {

		Jmm myJmm = new Jmm(new StringReader(jmmCode));
		try {
    		SimpleNode root = myJmm.Program(); // returns reference to root node

//    		root.dump(""); // prints the tree on the screen

			try {
				FileWriter myWriter = new FileWriter("./out.json");
				myWriter.write(root.toJson());
				myWriter.close();
				System.err.println("Successfully wrote to the file.");
			} catch (IOException e) {
				System.err.println("An error occurred.");
				e.printStackTrace();
			}
//			System.out.println(root.toJson());
//			if(myJmm.reports.size() != 0) {
//				for(Report r: myJmm.reports) //prints errors
//	            	System.out.println(r.toString());
//				throw new ParseException();
//			}

			return new JmmParserResult(root, myJmm.reports);
		} catch (ParseException e) {
			myJmm.reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, e.currentToken.beginLine, e.currentToken.beginColumn, e.getMessage()));
		} catch (Exception e) {
			myJmm.reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, -1, -1, e.getMessage()));
		}

		return new JmmParserResult(null, myJmm.reports);
	}

}
