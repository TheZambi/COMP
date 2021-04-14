
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;
import java.io.StringReader;

public class SyntacticPhase implements JmmParser {

	public JmmParserResult parse(String jmmCode) {

		Jmm myJmm = new Jmm(new StringReader(jmmCode));
		try {
    		SimpleNode root = myJmm.Program(); // returns reference to root node

    		root.dump(""); // prints the tree on the screen

//			if(myJmm.reports.size() != 0) {
//				for(Report r: myJmm.reports) //prints errors
//	            	System.out.println(r.toString());
//				throw new ParseException();
//			}

			return new JmmParserResult(root, myJmm.reports);
		} catch(ParseException e) {
			myJmm.reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, -1, "Parse failed with exception"));
//			throw new RuntimeException("Error while parsing", e);
		}

		return new JmmParserResult(null, myJmm.reports);
	}

}
