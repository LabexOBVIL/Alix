<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="prelude.jsp" %>
<%@ page import="alix.lucene.search.Doc" %>
<%@ page import="alix.util.Top" %>

<%!final static HashSet<String> DOC_SHORT = new HashSet<String>(Arrays.asList(new String[] {Alix.ID, Alix.BOOKID, "bibl"}));
public String results(TopDocs docs, IndexReader reader, int docSrc) throws  IOException
{
  StringBuilder out = new StringBuilder();
  ScoreDoc[] hits = docs.scoreDocs;
  for (int i = 0, len = hits.length; i < len; i++) {
    int docId = hits[i].doc;
    if (docSrc == docId) continue;
    Document doc = reader.document(docId, DOC_SHORT);
    out.append("<li>");
    out.append("<a href=\"?docid="+docId+"\">");
    out.append(doc.get("bibl"));
    out.append("</a>");
    out.append("</li>\n");
  }
  return out.toString();
}%>
<%
  /**
 * display a doc from the index.
 * Different case
 *  — direct read of a docid
 *  — find a doc by id field
 *  — query with an index order
 */

// params for the page

int docId = tools.getInt("docid", -1); // get doc by lucene internal docId or persistant String id
String id = tools.getString("id", null);
String q = tools.getString("q", null); // if no doc, get params to navigate in a results series
String sort = tools.getString("sort", null);
int start = tools.getInt("start", 1);
if (request.getParameter("prev") != null) { // if submit prev
  start = tools.getInt("prevn", start);
}
else if (request.getParameter("next") != null) { //if submit next
  start = tools.getInt("nextn", start);
}

// global variables
Corpus corpus = (Corpus)session.getAttribute(corpusKey);
Doc doc = null;
TopDocs topDocs = null;

// try to populate globals with params

try { // load full document
  if (id != null) doc = new Doc(alix, id);
  else if (docId >= 0) {
    doc = new Doc(alix, docId);
    id = doc.id();
  }
}
// doc not found
catch (IllegalArgumentException e) {
  id = null;
}
// if a query, provide navigation in documents
if (q != null) {
  topDocs = getTopDocs(pageContext, alix, corpus, q, sort);
  ScoreDoc[] hits = topDocs.scoreDocs;
  // ? a no result reponse caches ? Quite idiot, but...
  if (hits.length == 0) {
    topDocs = null;
    start = 0;
  }
  else {
    if (start < 1 || (start - 1) >= hits.length) start = 1;
    if (doc == null) {
      docId = hits[start - 1].doc;
      doc = new Doc(alix, docId); // should be right
      id = doc.id();
    }
  }
}

// bibl ref with no tags
String title = "";
if (doc != null) title = ML.detag(doc.doc().get("bibl"));
%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title><%=title%> [Obvil]</title>
    <link href="../static/vendors/teinte.css" rel="stylesheet"/>
    <link href="../static/obvil.css" rel="stylesheet"/>
    <script>
<%
if (doc != null) { // document id is verified, give it to javascript 
  out.println("var docLength="+doc.length(TEXT)+";");
  out.println("var id=\""+doc.id()+"\";");
}
%>
    </script>
    <script src="../static/js/common.js">//</script>
  </head>
  <body class="document">
  <%--  <a title="Comparer ce document" href="comparer?leftid=<%=id%>" target="_top" class="goright">⮞</a> --%>
  <%
  if (doc != null) {
    out.println("<header class=\"biblbar\" title=\""+title+"\">");
    out.print("<a href=\"#\" class=\"bibl\">");
    out.println(doc.doc().get("bibl"));
    out.print("</a>");
    out.println("</header>");
  }
  %>
  <main>
      <form id="qform" action="#">
        <input type="submit"
       style="position: absolute; left: -9999px; width: 1px; height: 1px;"
       tabindex="-1" />
        <input id="q" name="q" value="<%=Jsp.escape(q)%>" autocomplete="off" type="hidden"/>
        <select name="sort" onchange="this.form.submit()" title="Ordre">
            <option/>
            <%= sortOptions(sort) %>
        </select>
        <%
          if (topDocs != null && start > 1) {
              out.println("<input type=\"hidden\" name=\"prevn\" value=\""+(start - 1)+"\"/>");
              out.println("<button type=\"submit\" name=\"prev\">◀</button>");
            }
        %>
        <input id="start" name="start" value="<%=start%>" autocomplete="off" size="1"/>
               <%
        if (topDocs != null) {
          long max = topDocs.totalHits.value;
          out.println("<span class=\"hits\"> / "+ max  + "</span>");
          if (start < max) {
            out.println("<input type=\"hidden\" name=\"nextn\" value=\""+(start + 1)+"\"/>");
            out.println("<button type=\"submit\" name=\"next\">▶</button>");
          }
        }
        %>
      </form>
    <%
    if (doc != null) {
      out.println("<div class=\"heading\">");
      out.println(doc.doc().get("bibl"));
      out.println("</div>");
      out.println( alix.qTermList(q, TEXT));
      // hilite
      if (!"".equals(q)) {
        String[] terms = alix.qTermList(TEXT, q).toArray();
        out.print(doc.hilite(TEXT, terms));
      }
      else {
        out.print(doc.doc().get(TEXT));
      }
    }
    %>
    </main>
    <script src="../static/js/doc.js">//</script>
  </body>
</html>
