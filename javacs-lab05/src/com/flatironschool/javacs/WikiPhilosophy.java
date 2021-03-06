package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.net.UnknownHostException;
import java.lang.IllegalArgumentException;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;
import org.jsoup.*;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Set<String> links = new HashSet<String>();
		links = Philosophy(url, links);
		System.out.println(links);
	}
		
        // some example code to get you started
	public static Set<String> Philosophy (String url, Set<String> listLink){

		Elements paras;
		try {
			paras = wf.fetchWikipedia(url);
		} catch (IOException e){
			System.out.println("Not valid url!");
			return listLink;
		}
		
		
		//loop through all paragraph elements
		for (int i = 0; i < paras.size(); i++){
			Stack<Character> paren = new Stack<Character>();
			Element para = paras.get(i);
			
			Iterable<Node> iter = new WikiNodeIterable(para);
			Elements allLinks = para.getElementsByAttribute("href");

			//iterate with nodes
			for (Node node: iter) {
				//get texts from nodes
				String text;
				if (!(node instanceof TextNode)){
					Element test = (Element) node;
					text = test.ownText();
				} else {
					TextNode temp = (TextNode) node;
					text = temp.text();
				}

				boolean validLink = true;
				//get links from nodes (if exist)
				String current = node.attr("abs:href");
				
				for (Element elem: allLinks){
					Node temp = (Node) elem;
					if (elem.attr("abs:href").equals(current)){
						//checking italics
						Elements ancestors = elem.parents();
						if (ancestors.hasAttr("i") || ancestors.hasAttr("em")){
							validLink = false;
							System.out.println("Link is in italics!");
							break;
						}

						//checking parenthesis
						if (paren.size() != 0){
							validLink = false;
							System.out.println("Link is in parenthesis!");
							break;
						}
					}
				}

				//stack to keep track of parens
				for (int j = 0; j < text.length(); j++){
					if (text.charAt(j)=='('){
						paren.push('(');
					} else if (text.charAt(j) == ')'){
						paren.pop();
					}
				}

				if (!validLink){
					break;
				}

				if (!listLink.contains(current)){
					listLink.add(current);
					System.out.println(current);
					if (current.equals("https://en.wikipedia.org/wiki/Philosophy")){
						System.out.println("Succeed!");
						return listLink;
					} else {
						try {
							Connection newConn = Jsoup.connect(url);
							return Philosophy(current, listLink);
						} catch (IllegalArgumentException e) {
							continue;
						}
					}
				} 
			}
		}
		System.out.println("Failed!");
		return listLink;
	}
}
