
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;
import java.io.StringReader;

public class JmmSyntacticResults implements JmmParser {



	public JmmParserResult parse(String jmmCode) {
		
		try {
			Jmm myJmm = new Jmm(new StringReader(jmmCode));
    		SimpleNode root = myJmm.Program(); // returns reference to root node
            	
//    		root.dump(""); // prints the tree on the screen

//			for(Report r: myJmm.reports) //prints errors in json format
//	            System.out.println(r.toJson());

			if(myJmm.reports.size() != 0)
				throw new ParseException();

    		return new JmmParserResult(root, myJmm.reports);
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
//        if (args[0].contains("fail")) {
//            throw new RuntimeException("It's supposed to fail");
//        }
		var fileContents = SpecsIo.read("./test.txt");
        new JmmSyntacticResults().parse(fileContents);
    }


}