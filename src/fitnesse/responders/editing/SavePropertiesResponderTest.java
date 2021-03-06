// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import fitnesse.wiki.mem.InMemoryPage;
import util.RegexTestCase;

public class SavePropertiesResponderTest extends RegexTestCase {
  private WikiPage root;

  private MockRequest request;

  private WikiPage page;

  private PageCrawler crawler;

  private WikiPage linker;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  private void createRequest() throws Exception {
    page = crawler.addPage(root, PathParser.parse("PageOne"));

    request = new MockRequest();
    request.addInput("PageType", "Test");
    request.addInput("Properties", "on");
    request.addInput("Search", "on");
    request.addInput("RecentChanges", "on");
    request.addInput(PageData.PropertyPRUNE,"on");
    request.addInput(PageData.PropertySECURE_READ, "on");
    request.addInput("Suites", "Suite A, Suite B");
    request.addInput("HelpText", "Help text literal");
    request.setResource("PageOne");
  }

  public void tearDown() throws Exception {
  }

  public void testResponse() throws Exception {
    createRequest();

    Responder responder = new SavePropertiesResponder();
    Response response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    PageData data = page.getData();
    assertTrue(data.hasAttribute("Test"));
    assertTrue(data.hasAttribute("Properties"));
    assertTrue(data.hasAttribute("Search"));
    assertFalse(data.hasAttribute("Edit"));
    assertTrue(data.hasAttribute("RecentChanges"));
    assertTrue(data.hasAttribute(PageData.PropertySECURE_READ));
    assertFalse(data.hasAttribute(PageData.PropertySECURE_WRITE));
    assertTrue(data.hasAttribute(PageData.PropertyPRUNE));
    assertEquals("Suite A, Suite B", data.getAttribute(PageData.PropertySUITES));
    assertEquals("Help text literal", data.getAttribute(PageData.PropertyHELP));

    assertEquals(303, response.getStatus());
    assertEquals("PageOne", response.getHeader("Location"));
  }

}
