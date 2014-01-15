package eu.sqooss.impl.service.webadmin;

/**
 * This is a class whose sole purpose is to provide a useful API from
 * within Velocity templates for the translation functions offered by
 * the AbstractView. Only one object needs to be created, and it
 * forwards all the label(), message() and error() calls to the translation
 * methods of the view.
 */
public class TranslationProxy {
    AbstractView view;
	public TranslationProxy(AbstractView view) { 
    	this.view = view;
    }
    
    /** Translate a label */
    public String label(String s) {
        return view.getLbl(s);
    }
    
    /** Translate a (multi-line, html formatted) message */
    public String message(String s) {
        return view.getMsg(s);
    }
    
    /** Translate an error message */
    public String error(String s) {
        return view.getErr(s);
    }
}