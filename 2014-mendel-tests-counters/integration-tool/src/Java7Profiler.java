import java.io.*;
import java.util.*;

import org.antlr.v4.parse.GrammarTreeVisitor.tokenSpec_return;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CommonTokenStream;

public class Java7Profiler extends Java7BaseListener {
	private final CommonTokenStream ts;

	public final Map<Integer, String> incOfMethod = new HashMap<Integer, String>();
	public final List<String> counters = new ArrayList<String>();

    private final boolean useTimeoutChecker;

	public Java7Profiler(CommonTokenStream tokens, boolean useTimeoutChecker)
			throws FileNotFoundException {
		this.ts = tokens;
		this.useTimeoutChecker = useTimeoutChecker;
	}

    private String incrementFor(String counter) {
        if (useTimeoutChecker) {
            return "if ((++" + counter + " & 262143) == 0) TimeoutChecker.check();";
        } else {
            return "++" + counter + ";";
        }
    }

	@Override
	public void enterMethodDeclaration(Java7Parser.MethodDeclarationContext ctx) {
	    String counter = "counter$" + counters.size();
	    counters.add(counter);
		if (!ctx.getChild(1).getText().equals("main")) {
    		incOfMethod.put(ctx.methodBody().getStart().getLine() + 1, incrementFor(counter));
		}
	}

	@Override
	public void enterForState(Java7Parser.ForStateContext ctx) {
	    String counter = "counter$" + counters.size();
	    counters.add(counter);
		if (ctx.statement().getText().charAt(0) != '{') {
			incOfMethod.put(ctx.statement().getStart().getLine(), "{ " + incrementFor(counter));
			incOfMethod.put(ctx.statement().getStop().getLine() + 1, "}");
		} else {
			incOfMethod.put(ctx.statement().getStart().getLine() + 1, incrementFor(counter));
	    }
	}

	@Override
	public void enterWhileState(Java7Parser.WhileStateContext ctx) {
	    String counter = "counter$" + counters.size();
	    counters.add(counter);
		if (ctx.statement().getText().charAt(0) != '{') {
			incOfMethod.put(ctx.statement().getStart().getLine(), "{ " + incrementFor(counter));
			incOfMethod.put(ctx.statement().getStop().getLine() + 1, "}");
		} else {
		    incOfMethod.put(ctx.statement().getStart().getLine() + 1, incrementFor(counter));
		}
	}

	@Override
	public void enterDoWhileState(Java7Parser.DoWhileStateContext ctx) {
	    String counter = "counter$" + counters.size();
	    counters.add(counter);
		if (ctx.statement().getText().charAt(0) != '{') {
			incOfMethod.put(ctx.statement().getStart().getLine(), "{ " + incrementFor(counter));
			incOfMethod.put(ctx.statement().getStop().getLine() + 1, "}");
		} else {
		    incOfMethod.put(ctx.statement().getStart().getLine() + 1, incrementFor(counter));
		}
	}
}
