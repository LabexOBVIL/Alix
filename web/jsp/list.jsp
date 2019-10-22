<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="common.jsp" %>
<%
String pageType = (String)request.getAttribute("pageType");

IndexSearcher searcher = alix.searcher();
IndexReader reader = alix.reader();
Doc refDoc = null;
int refDocId = getParameter(request, "refdocid", -1);
int fromDoc = getParameter(request, "fromdoc", -1);
int fromScore = getParameter(request, "rfromscore", 0);
final int hpp = 100;
ScoreDoc[] hits = null;
Query query = null;


if (refDocId > 0) {
  refDoc = new Doc(alix, refDocId);
  Top<String> topTerms = refDoc.theme(TEXT);
  query = Doc.moreLikeThis(TEXT, topTerms, 50);
}
else if (!"".equals(q)) {
}

if (query != null) {
}
%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Documents similaires [Obvil]</title>
    <link href="../static/obvil.css" rel="stylesheet"/>
  </head>
  <body class="results">
    <header>
<%
if ("bibl".equals(pageType)) {
  out.println("<p>Chercher un document par ses métadonnées.</p>");

}
else if (refDoc != null) {
  out.println("<h1>Document similaires</h1>");
  out.println("<b>Documents similaires à :</b>");
  out.println(refDoc.fields().get("bibl"));
}
else {
  // out.println("<h1>Document similaires</h1>");
  // out.println("<p>À gauche, choisissez un document pour comparer</p>");
}
%>
    </header>
    <form>
      <%
if (refDoc != null) {
  out.println("<input type=\"hidden\" name=\"refdocid\" value=\"" +refDocId+"\"/>");
}
else {
  out.println("<input size=\"50\" type=\"text\" id=\"q\" onfocus=\"var len = this.value.length * 2; this.setSelectionRange(len, len); \" autofocus spellcheck=\"false\"  name=\"q\" value=\"" +q+"\"/>");
  // out.println("<br/>" + query);
}

// go next
if (hits != null && false) {
  out.println("<input type=\"hidden\" name=\"fromdoc\" value=\""+hits[hpp - 1].doc+"\"/>");
  out.println("<input type=\"hidden\" name=\"fromscore\" value=\""+hits[hpp - 1].score+"\"/>");
}
      %>
    </form>

    <main>
<%--
<jsp:include page="file.jsp" >
<jsp:param name="firstname" value="Chaitanya" />
<jsp:param name="middlename" value="Pratap" />
<jsp:param name="lastname" value="Singh" />
</jsp:include>

--%>

    </main>
    <script src="../static/js/list.js">//</script>
  </body>
</html>