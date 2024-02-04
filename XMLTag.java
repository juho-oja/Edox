import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class XMLTag {
/*
	public static void main(String[] args) {
		XMLTag testi = new XMLTag(new File("edo22.xml"));
		System.out.println(testi.text());
		for (int i = 0; i < testi.nsub(); i++);
	}
*/
	private String name;
	private String text;
//	private ArrayList<XMLAttribute> attributes;

	private ArrayList<String> attr_names, attr_vals;

	private ArrayList<XMLTag> sub;
	private XMLTag parent;
	private FileWriter outfile;
	private boolean debug;
	private int iter;

/*
	private class XMLAttribute {
		public String attribute;
		public String value;

		public XMLAttribute(String p_name, String p_value) {
			attribute = new String(p_name);
			value = new String(p_value);
		}
	}
*/
	public XMLTag() {
	//	System.out.println("*** XMLTag: creating a new empty tag");
	//	attributes = null;
		attr_names = null;
		attr_vals = null;
		sub = null;
		outfile = null;
		debug = false;
		parent = null;
		iter = 0;
	}

	public XMLTag(String p_name) {
	//	System.out.println("*** XMLTag: creating new tag with name " + p_name);
		name = new String(p_name);
	//	attributes = null;
		attr_names = null;
		attr_vals = null;
		sub = null;
		outfile = null;
		debug = false;
		parent = null;
		iter = 0;
	}

	public XMLTag(File p_file) {
		new XMLFile(p_file.toString());
	}

	public void loadFromFile(String p_file) {
		new XMLFile(p_file);
	}

	public static XMLTag fromFile(String p_file) {
		XMLTag ret = new XMLTag();
		ret.loadFromFile(p_file);
		return ret;
	}

	public void setName(String p_name) {
		// check that no invalid characters are present
		if (debug) System.out.println("*** XMLTag: Setting name: " + p_name);
		name = new String(p_name);
	}

	public void setText(String p_text) {
		if (debug) System.out.println("*** XMLTag: Setting text:" + p_text);
		text = new String(p_text);
	}

	public void deleteText() {
		text = null;
	}

	public String getName() {
		return new String(name);
	}

	public String name() {
		return new String(name);
	}

	public void addAttribute(String p_name, String p_value) {
		if (attr_names == null) {
			attr_names = new ArrayList<String>();
			attr_vals = new ArrayList<String>();
		}

		if (debug) System.out.println("*** XMLTag: Adding attribute \"" + p_name + "\" with value \"" + p_value + "\" to " + name + "." + this.hashCode());
		try {
			attr_names.add(p_name);
			attr_vals.add(p_value);
		} catch (Exception exc) {
			if (debug) System.out.println("Error: " + exc.getMessage());
		}
	}

	public void printAttr() {
		for (int i = 0; i < attr_names.size(); i++)
			System.out.println(attr_names.get(i) + " = " + attr_vals.get(i));
	}

	public String attr(String p_attr) {
		if (attr_names == null) return null;
		for (int i = 0; i < attr_names.size(); i++) {
			if (attr_names.get(i).equals(p_attr))
				return new String(attr_vals.get(i));
		}
		return null;
	}

	public XMLTag subwa(String p_name, String p_attr) {
		if (p_name == null || p_attr == null) return null;
		
		for (int i = 0; i < sub.size(); i++) {
			if (sub.get(i).name.equals(p_name)) {
				for (int j = 0; j < sub.get(i).attr_names.size(); j++)
					if (sub.get(i).attr_names.get(j).equals(p_attr)) return sub.get(i);
			}
		}
		return null;
	}

	public String suba(String p_name, String p_attr) {
		if (sub == null || p_name == null || p_attr == null) return null;
		int i = 0;
		boolean success = false;

		for (; i < sub.size(); i++) {
		//	System.out.println(String.format("Testing %s against %s", p_name, sub.get(i).name));
			if (sub.get(i).name.equals(p_name)) {
				success = true;
				break;
			}
		}
		if (success == false) return null;
	//	System.out.println("Breaking...");

		if (sub.get(i).attr_names == null) {
	//		System.out.println("Tag has no attributes");
			return null;
		}

		for (int j = 0; j < sub.get(i).attr_names.size(); j++) {
	//		System.out.println(String.format("  Testing %s against %s", p_attr, sub.get(i).attributes.get(j).attribute));
			if (sub.get(i).attr_names.get(j).equals(p_attr))
				return new String(sub.get(i).attr_vals.get(j));
		}

		return null;
	}

	public String suba(int p_i, String p_attr) {
		if (sub == null || p_i < 0 || p_i > sub.size() || p_attr == null) return null;

		if (sub.get(p_i).attr_names == null) return null;

		for (int j = 0; j < sub.get(p_i).attr_names.size(); j++)
			if (sub.get(p_i).attr_names.get(p_i).equals(p_attr))
				return new String(sub.get(p_i).attr_vals.get(j));

		return null;
	}

/*
	public String attrn(String p_attr) {
		for (int iter++; iter < attributes.size(); iter++) {
			if (attributes.get(i).attribute.equals(p_attr)) {
				return new String(attributes.get(i).value);
			}
		}
		iter = 0;
		return null;
	}
*/
	public void addSubtag(XMLTag p_tag) {
		if (p_tag.name.equals("")) return;
		if (sub == null) sub = new ArrayList<XMLTag>();
	//	if (debug) System.out.println("*** XMLTag: Adding subtag \"" + p_tag.name + "\" to \"" + name + "\"");
		try {
			p_tag.parent = this;
			sub.add(p_tag);
		} catch (Exception exp) {
			throw new IllegalArgumentException("*** XMLTag: cannot add subtag...");
		}
	}

	public XMLTag parent() {
		return parent;
	}

	public XMLTag sub(String p_name) {	// grabs the first tag with the name
		for (int i = 0; i < sub.size(); i++)
			if (sub.get(i).name.equals(p_name)) return sub.get(i); 
		return null;
	}

	public XMLTag first(String p_name) {	// identical to above, but sets iter
		for (int i = 0; i < sub.size(); i++)
			if (sub.get(i).name.equals(p_name)) {
				iter = i;
				return sub.get(i); 
			}
		return null;
	}

	public XMLTag last(String p_name) {
		for (int i = sub.size() - 1; i >= 0; i--)
			if (sub.get(i).name.equals(p_name)) {
				iter = i;
				return sub.get(i);
			}
		return null;
	}

	public XMLTag next(String p_name) {
		if (sub == null) return null;
		for (iter++; iter < sub.size(); iter++)
			if (sub.get(iter).name.equals(p_name)) return sub.get(iter); 
		iter = 0;	// if more was not found, reset iterator and return null;
		return null;
	}

	public XMLTag previous(String p_name) {
		for (iter--; iter >= 0; iter--)
			if (sub.get(iter).name.equals(p_name)) return sub.get(iter);
		iter = 0;	// will not loop back to the end
		return null;
	}

	public void reset() {
		iter = 0;
	}

	public int index() {
		return iter;
	}

	public XMLTag sub(int p_index) {
	//	System.out.println("Asking for index " + p_index + ", max " + sub.size());
		if (p_index >= 0 && p_index < sub.size()) return sub.get(p_index);
		return null;
	}
	
	// Subtag With Attribute Value
	public XMLTag subwav(String p_tag, String p_attr, String p_val) {	// first (only) subtag with given attribute value
		if (sub == null || p_tag == null || p_attr == null || p_val == null) return null;

		for (int i = 0; i < sub.size(); i++)
			if (sub.get(i).name.equals(p_tag) && sub.get(i).attr_names != null)
				for (int j = 0; j < sub.get(i).attr_names.size(); j++)
					if (sub.get(i).attr_names.get(j).equals(p_attr) &&
						sub.get(i).attr_vals.get(j).equals(p_val)) return sub.get(i);
					
		return null;
	}

	public XMLTag subwav(String p_attr, String p_val) {	// same as above but disregards tag name (assumed to be the same or not relevant)
		if (sub == null || p_attr == null || p_val == null) return null;

		for (int i = 0; i < sub.size(); i++) {
			if (sub.get(i).attr_names == null) continue;
			for (int j = 0; j < sub.get(i).attr_names.size(); j++)
				if (sub.get(i).attr_names.get(j).equals(p_attr) &&
					sub.get(i).attr_vals.get(j).equals(p_val)) return sub.get(i);
		}
		return null;
	}

	public String text() {
		if (text != null) return new String(text);
		else return null;
	}

	public void printSubtags() {
		for (int k = 0; k < sub.size(); k++)
			System.out.println(sub.get(k).name);
	}

	public int nsub() {
		if (sub != null) return sub.size();
		else return 0;
	}

	public int nattr() {
		if (attr_names != null) return attr_names.size();
		else return 0;
	}

//	Removes text from all elements that contain subtags
	public void removeText() {
		if (sub != null) {
			text = null;
			for (int i = 0; i < sub.size(); i++)
				sub.get(i).removeText();
		}
	}
	
	public boolean removeAttribute(String p_attr) {	// returns true if successful
		if (p_attr != null && !(p_attr.equals(""))) {
	//	System.out.println("*** XMLTag: removeAttribute() - attempting to remove attribute " + p_attr);
			for (int i = 0; i < attr_names.size(); i++)
				if (attr_names.get(i).equals(p_attr)) {
					attr_names.remove(i); attr_vals.remove(i);
					return true;
				}
		}
		return false;
	}

	public void copySubtagsShallow(XMLTag p_src) {
		if (p_src == null) return;
		for (int i = 0; i < p_src.nsub(); i++)
			addSubtag(p_src.sub(i));
	}

	public void copySubtagsDeep(XMLTag p_src) {

	}

	static public XMLTag copyTagDeep(XMLTag p_src) {
		// first create the base tag
		// get attributes of the base tag
		// do subtags recursively
		if (p_src == null) return null;

		XMLTag base = new XMLTag(p_src.name);	// base tag
		if (p_src.attr_names != null) {			// copy attributes
			base.attr_names = new ArrayList<String>();
			base.attr_vals = new ArrayList<String>();

			for (int i = 0; i < p_src.attr_names.size(); i++) {
				base.attr_names.add(p_src.attr_names.get(i));
				base.attr_vals.add(p_src.attr_vals.get(i));
			}
		}

		if (p_src.text != null) base.text = new String(p_src.text);// copy text

		if (p_src.sub != null) { // copy subtags
			base.sub = new ArrayList<XMLTag>();
			XMLTag copy;
			for (int i = 0; i < p_src.sub.size(); i++) {
				copy = copyTagDeep(p_src.sub.get(i), base);
				base.sub.add(copy);
			}
		}
		return base;
	}

 // parent structure needs to be recreated
	static public XMLTag copyTagDeep(XMLTag p_src, XMLTag p_parent) {
		// first create the base tag
		// get attributes of the base tag
		// do subtags recursively
		if (p_src == null) return null;

		XMLTag base = new XMLTag(p_src.name);	// base tag
		if (p_src.attr_names != null) {			// copy attributes
			base.attr_names = new ArrayList<String>();
			base.attr_vals = new ArrayList<String>();

			for (int i = 0; i < p_src.attr_names.size(); i++) {
				base.attr_names.add(p_src.attr_names.get(i));
				base.attr_vals.add(p_src.attr_vals.get(i));
			}
		}

		if (p_src.text != null) base.text = new String(p_src.text);	// copy text
		if (p_parent != null) base.parent = p_parent;				// set parent

		if (p_src.sub != null) { // copy subtags
			base.sub = new ArrayList<XMLTag>();
			for (int i = 0; i < p_src.sub.size(); i++)
				base.sub.add(copyTagDeep(p_src.sub.get(i), base));
		}
		return base;
	}

	//	private void copySubtag
/*
	public int iter() {
		return iter;
	}
*/
	public boolean removeSubtag(int p_ind) {
		if (p_ind >= 0 && p_ind < sub.size() - 1) {
			sub.remove(p_ind);
			return true;
		}
		return false;
	}

	public boolean removeSubtag(XMLTag p_tag) {
		for (int i = 0; i < sub.size(); i++) {
			if (sub.get(i) == p_tag) {
				sub.remove(i);
				return true;
			}
		}
		return false;
	}

	public void replaceSubtag(XMLTag p_tag, int p_ind) {
		if (p_ind < 0 || p_ind >= sub.size() || p_tag == null) return;
		sub.set(p_ind, p_tag);
	}

	public void replaceSubtag(XMLTag p_old, XMLTag p_new) {
		for (int i = 0; i < sub.size(); i++) {
			if (sub.get(i) == p_old) {
				sub.set(i, p_new);
			}
		}
	}
/*
	public XMLTag next(String p_label) {

	}*/

	public String attrN(int p_index) {	// name of attribute index
		if (p_index >= 0 && p_index < attr_names.size())
			return new String(attr_names.get(p_index));
		else return null;
	}

	public String attrV(int p_index) {	// value of attribute index
		if (p_index >= 0 && p_index < attr_names.size())
			return new String(attr_vals.get(p_index));
		else return null;
	}

	public String attrV(String p_str) {
		if (p_str == null || p_str.equals("")) return null;
		for (int i = 0; i < attr_names.size(); i++)
			if (attr_names.get(i).equals(p_str)) return new String(attr_vals.get(i));
		return null;
	}

	public void addAttr(String p_name, String p_value) {
		if (p_name == null || p_value == null) return;

		if (attr_names == null) {
			attr_names = new ArrayList<String>();
			attr_vals = new ArrayList<String>();
		}
		attr_names.add(p_name);
		attr_vals.add(p_value);
	}
//	public void setText(String p_text) {
//		if (p_text != null) text = new String(p_text);
//	}

	public String headerText() {
		StringBuffer adder = new StringBuffer();
		adder.append(String.format("<%s ", name));
		for (int i = 0; i < attr_names.size(); i++)
			adder.append(String.format("%s=\"%s\" ", attr_names.get(i), attr_vals.get(i)));
		adder.append("/>");
		return adder.toString();
	}

	public void writeToFile(String pathfilename) {
		try {
			outfile = new FileWriter(pathfilename);
				tagToOutfile(this, 0);
			outfile.close();
			outfile = null;
		} catch (Exception e) {
			System.out.println("*** XMLTag: writeToFile() error - " + e.getMessage());
		}
	}

	public void setDebug() {
		debug = true;
	}

	private void tagToOutfile(XMLTag writag, int level) {
		// tag start
		StringBuffer temp = new StringBuffer();
		// add indentation
	//	if (debug) System.out.println("*** XMLTag: Attempting to write start tag for " + writag.name + "...");
		temp.append(makeIndent(level) + String.format(Locale.ROOT, "<%s", writag.name));
	//	if (debug) System.out.println("*** XMLTag: Start tag written...");
	// 	attributes...
		if (writag.attr_names != null) {
	//		if (debug) System.out.println("*** XMLTag: tagToOutfile() - Attempting to write " + writag.name + " attributes...");
			for (int i = 0; i < writag.attr_names.size(); i++) {
	/*			if (debug) System.out.println("*** XMLTag: tagToOutfile() - attribute " +
					i + ": name = " + writag.attributes.get(i).attribute + ", value = " +
					writag.attributes.get(i).value);*/

				temp.append(String.format(Locale.ROOT, " %s=\"%s\"",
					writag.attr_names.get(i), writag.attr_vals.get(i))); 
			}
	//		if (debug) System.out.println("*** XMLTag: tagToOutfile() - done writing attributes...");
		}

	// if no subtags or text, no separate closing tag
		if (writag.sub == null && writag.text == null) {
	//		if (debug) System.out.println("*** XMLTag. tagToOutfile - closing empty tag " + writag.name);
			temp.append(" />\n");
			writeString(temp.toString());
			return;	// DONE
		}

	// close the start tag
		temp.append(">\n");
		writeString(temp.toString());

	// if subtags, write them, else write text
		if (writag.sub != null) {
			for (int i = 0; i < writag.sub.size(); i++)
				tagToOutfile(writag.sub.get(i), level + 1);
		} else {
		// Write text line by line
			StringBuffer textmod = new StringBuffer();
			textmod.append(makeIndent(level + 1));
			for (int j = 0; j < writag.text.length(); j++) {
				if (writag.text.charAt(j) == '\n' && (j != writag.text.length() - 1)) {
					textmod.append('\n');
					textmod.append(makeIndent(level + 1));
				}
				else textmod.append(writag.text.charAt(j));
			}
			textmod.append('\n');
			writeString(textmod.toString());
		}

	// write the closing tag
		writeString(makeIndent(level) + "</" + writag.name + ">\n");
	}

	private String makeIndent(int p_level) {
		if (p_level < 0 || p_level > 100) return null;
		StringBuffer temp = new StringBuffer();
		for (int i = 0; i < p_level; i++)
			temp.append('\t');
		return temp.toString();
	}

	private void writeString(String p_string) {
	//	if (debug) System.out.println("*** XMLTag: writeString() - writing string " + p_string);
		try {	// writing start tag at this point, before text or subtags
			outfile.write(p_string);
		} catch (Exception e) {
			System.out.println("*** XMLTag: writeString() error - " + e.getMessage());
			return;
		}
	}
/*
	public static void main(String[] args) {
		XMLTag testi = new XMLTag();
		testi.setDebug();
		testi.setName("testroot");
	//	testi.addAttr("id", "paskamaa");
		testi.setText("This is the text\nthat spans multiple\nlines and sh");
			XMLTag subtest = new XMLTag("testsub");
			subtest.addAttr("name", "pakkeli");
				XMLTag subsubtest = new XMLTag("testsubtest");
				subsubtest.setText("Vittustana");
				subtest.addSubtag(subsubtest);
			XMLTag subtest2 = new XMLTag("testsub2");
		//	subtest2.addAttr("type", "tokotti");
			subtest2.setText("Saatana se toimii vittu\nPERKELE!");
		testi.addSubtag(subtest);
		testi.addSubtag(subtest2);
		testi.writeToFile("temp/testxml2.xml");
	}
*/
	private class XMLFile {
	//	private XMLTag main_tag;
		private ArrayList<XMLTag> tags;
		private String xmldata;
	
		private StringBuffer collector;	// for stuff that does not need to be preserved across
										// recursive function calls 
	//	private StringBuffer text;
	//	private ArrayList<String> stack;
		private int si; // stack index
		private int i; // string counter, for storing position between recursive calls
	//	private String current;

	//	private boolean comment_on;
	//	private boolean tag_on;

		private File xmlfile;
	//	private Scanner reader;
	/*
		public class XMLAttribute {
			public String attribute;
			public String value;

			public XMLAttribute(String p_name, String p_value) {
				attribute = new String(p_name);
				value = new String(p_value);
			}
		}
	*/
		public XMLFile(String p_filename) {
		//	stack = new ArrayList<String>();
			collector = new StringBuffer(1024);
		//	text = new StringBuffer(16384);
		//	main_tag = new XMLTag();

			boolean exit = false;
			try {
				xmlfile = new File(p_filename);
				xmldata = new String(Files.readAllBytes(Paths.get(p_filename)));
			// look for tag on the first line
			/*	for (i = 0; i < xmldata.length() && !exit; i++) {
					if (xmldata.charAt(i) > 32) {*/
					//	System.out.println("Found the first non-whitespace character: " + xmldata.charAt(i));
			/*			for (; i < xmldata.length(); i++) {
							collector.append(xmldata.charAt(i));*/
	
					/*		if (xmldata.charAt(i) == '/') {
								break;
							}*/
			/*				if (xmldata.charAt(i) == '>') {
								exit = true;
								break;
							}*/
					/*		if (xmldata.charAt(i) <= 32) {
								break;
							}*/
					//	}
						//System.out.println(collector);
				//	}
					//collector.delete(0, collector.length());
			//	}

			//	System.out.println(collector);
			/*	if (!collector.toString().equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
					throw new Exception("Unknown or invalid xml format");*/
	
				skipWhitespace();
				readTag(XMLTag.this);
			/*	skipWhitespace();
				readTag(main_tag);*/

			} catch (Exception e) {
				System.out.println("XMLFile(): Invalid error: " + e.getMessage());
			}
		}

		private boolean checkTagEnd() {
//			if (xmldata.charAt(i) == '/' || xmldata.charAt(i) == '>' || xmldata.charAt(i) <= 32) return true;
			return false;
		}

		private void readTag(XMLTag fill_tag) {
		//	System.out.println("Reading new tag... " + xmldata.charAt(i));
			skipWhitespace();
			String tagname;
			XMLTag temp_tag = new XMLTag();
		//	if (xmldata.charAt(i) != '<') throw new IllegalArgumentException("Expected: < (no tag found)");
			if (xmldata.length() - i > 4)	// See if the tag is a comment
				if (xmldata.substring(i, i + 4).equals("<!--")) {
				//	System.out.println("Found a comment!");
					skipComment();
					skipWhitespace();
				}

		//	otherwise look for a tag
			collector.delete(0, collector.length());
			i++; // to move iterator forward from tag character
			collectUntilSpecial();
		//	System.out.println("Tag name: " + collector.toString());
		//	System.out.println("Stopped at character no. " + (int)xmldata.charAt(i) + " ( " + xmldata.charAt(i) + " )");
		//	Atttempt to get text
			fill_tag.setName(collector.toString());
		//	System.out.println("Tag name set to " + fill_tag.getName());

			if (xmldata.charAt(i) <= 32) {
			//	System.out.println("Tag might have an attribute");
				skipWhitespace();
				getAttributes(fill_tag);
			}

			if (xmldata.charAt(i) == '>') {
				try {
					collector.delete(0, collector.length());
					i++; // move iterator away from the '>' character
				//	System.out.println("i was " + i + " before skipping until line");
					skipUntilLine();
				//	System.out.println("Skipped until line to " + i);
					getText(fill_tag);
				//	fill_tag.setText();
				} catch (Exception exc) {
					System.out.println("Tag error: " + exc.getMessage());
					System.exit(1);
				}
			}

		//	for (; i < xmldata.length(); i++);
		}

		private void skipWhitespace() {
		//	System.out.println("Skipping whitespace..." + xmldata.charAt(i));
			for (; i < xmldata.length() - 1 && xmldata.charAt(i) <= 32; i++); // System.out.println("Skip counter " + i);
		//	System.out.println("Whitespace skipped..." + xmldata.charAt(i));
		}

		private void skipUntilLine() {
		//	System.out.println("Skipping until newline...");
			for (; i < xmldata.length() - 1 && xmldata.charAt(i) <= 32 && xmldata.charAt(i) != '\n'; i++);
		//	i++;
		}

		private void skipToNextTag() {
		//	System.out.println("Skipping to next tag...");
			for (; i < xmldata.length(); i++)
				if (xmldata.charAt(i) == '<') break;
			i++;
		}

		private void skipComment() {
		//	System.out.println("skipComment: starting at i = " + i);
			i += 4;
			for (; i < xmldata.length() - 4; i++) {
			//	System.out.println("c: " + xmldata.charAt(i));
				if (xmldata.charAt(i) == '-')
					if (xmldata.charAt(i + 1) == '-' && xmldata.charAt(i + 2) == '>') {
						i += 3;
						break;
					}

				if (xmldata.charAt(i) == '<')
					if (xmldata.substring(i, i + 4).equals("<!--")) skipComment();
			}
		//	System.out.println("skipComment: i at " + i);
		//	skipWhitespace();
		}

		private void getAttributes(XMLTag fill_tag) {
		//	System.out.println("Getting attributes...");
			for (; i < xmldata.length() - 1; i++) {
			//	System.out.println("\tRound " + i);
				skipWhitespace();
				if (xmldata.charAt(i) == '/' || xmldata.charAt(i) == '>') break;
	
				StringBuffer name = new StringBuffer(32);
			//	System.out.println("\tGetting attribute name " + i + ", char is " + xmldata.charAt(i));
				for (; i < xmldata.length() - 1; i++) {
					if (xmldata.charAt(i) == '=') {
						i++;
						skipWhitespace();
						if (xmldata.charAt(i) == '"') break;
						else throw new IllegalArgumentException("invalid use of character =");
					}
					name.append(xmldata.charAt(i));
				}
		//	System.out.println("\tHave attribute name " + name);
				int k;
				for (k = name.length() - 1; name.charAt(k) <= 32; k--);
				name.setLength(k + 1);

				StringBuffer value = new StringBuffer(32);
				for (i += 1; i < xmldata.length(); i++) {
					if (xmldata.charAt(i) == '"') break;
					value.append(xmldata.charAt(i));
				}
		//	System.out.println("Attribute " + name.toString() + " with value " + value.toString());
				fill_tag.addAttribute(name.toString(), value.toString());
			}
	//	fill_tag.printAttr();
		}

		private void getText(XMLTag p_tag) {
		//	System.out.println("Getting text for " + p_tag.getName());
	/*		boolean chars_on_line = false;*/
			int starti = i;
			String midcollect;
			for (; i < xmldata.length() - 3; i++) { // collect all the text first, with tags and all
			//	System.out.println("gText: " + xmldata.charAt(i) + ", " + i);
				if (xmldata.charAt(i) == '<') {
				//	System.out.println("Found <");
					if (xmldata.charAt(i + 1) == '/') {
				//	System.out.println("Found /");
						i += 2;
					//	int tagi = i;
					//	System.out.println("Attempting to read the tag ending name...");
						String endtemp = peekTagName();
					//	System.out.println("Comparing " + endtemp + " to " + p_tag.getName());
						if (endtemp.equals(p_tag.getName())) {
						//	System.out.println("\tBreaking...");
							break;
						}
						else collector.append("</");
					}

				//	System.out.println("Testing for comment, i = " + i);
					if (xmldata.substring(i, i + 4).equals("<!--")) {
					// remove tabs from collector
						int k = collector.length() - 1;
						for (; k >= 0 && collector.charAt(k) == 9; k--);
						collector.setLength(k + 1);
						skipComment();
						skipUntilLine();
						i--; // skip until line moves it one ahead of where it should (for this purpose)
						continue;
					} else {
					//	System.out.println("No comment found...");
					}
				}
				collector.append(xmldata.charAt(i));
			//	System.out.println("Added \"" + xmldata.charAt(i) + "\" to collector, i = " + i);
	
			/*	if (xmldata.charAt(i) == '\t') {
					if (chars_on_line) collector.append('\t');
				} else collector.append(xmldata.charAt(i));*/
			/*	if (xmldata.charAt(i) > 32) chars_on_line = true;
				if (xmldata.charAt(i) == '\n') chars_on_line = false;*/
			}
			int j;
			for (j = collector.length() - 1; j >= 0 && collector.charAt(j) <= 32; j--);
			if (j > -1) collector.setLength(j + 1);
		//	System.out.println("\nCollected text: \n" + collector.toString());
			p_tag.setText(collector.toString());
	
		// GET SUBTAGS
			i = starti; // back to the beginning
		//	System.out.println("Getting subtags...");
			for (; i < xmldata.length() - 4; i++) {
				skipWhitespace();
				if (xmldata.charAt(i) == '<' && xmldata.charAt(i + 1) != '/') {
					if (xmldata.substring(i, i + 4).equals("<!--")) {
						skipComment();
						continue;
					}

					XMLTag rec_tag = new XMLTag();
					readTag(rec_tag);
				//	System.out.println("Collected attributes for " + rec_tag.getName());
			//		rec_tag.printAttr();
					p_tag.addSubtag(rec_tag);
				}

				if (xmldata.charAt(i) == '<' && xmldata.charAt(i + 1) == '/') {
					i += 2;
				//	System.out.println("Found tag ending, breaking...");
					if (peekTagName().equals(p_tag.getName())) break;
				}
			}
		}

		private String peekTagName() {
		//	System.out.println("Peeking tag name...");
			int k = i;
			StringBuffer temp = new StringBuffer();
			for (; k < xmldata.length() - 1; k++) {
				if (xmldata.charAt(k) <= 32 || xmldata.charAt(k) == '>') break;
	
				if (xmldata.charAt(k) == '/')
					if (xmldata.charAt(k + 1) == '>') break;
					else continue;
			
				temp.append(xmldata.charAt(k));
			}
		//	System.out.println("Ending tag: " + temp);
			return temp.toString();
		}

		private void collectUntilSpecial() {
		//	System.out.println("Collecting until whitespace or special (/ < >)...");
			for (; i < xmldata.length(); i++) {
			//	System.out.println("C: " + xmldata.charAt(i));
				if (xmldata.charAt(i) <= 32) break;
				if (xmldata.charAt(i) == '/') break;
				if (xmldata.charAt(i) == '>' || xmldata.charAt(i) == '<') break;
				collector.append(xmldata.charAt(i));
			}
		//	System.out.println("\tDone collecting!");
		}

	/*	public XMLTag root() {
			return main_tag;
		}*/
	}
}
