package fit.decorator;

import java.text.ParseException;

import junit.framework.TestCase;
import fit.Counts;
import fit.Dispatcher;
import fit.Fixture;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.TestCaseHelper;
import fit.decorator.util.Timer;

public abstract class FixtureDecoratorTestCase extends TestCase {
  protected static final long ELAPSED = 20;
  protected Timer stopWatch = new Timer() {
    public void start() {
    }

    public long elapsed() {
      return ELAPSED;
    }
  };

  public void testShouldBeAbleToExecuteEncapsulatedFixture() throws ParseException {
    String fitPage = "<table>" + geDecoratorHTMLRow() + "<tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    int right = 1 + numberOfAssertionsOnDecorator();
    TestCaseHelper.assertCounts(TestCaseHelper.counts(right, 0, 0, 0), dispatcher.counts);
  }

  public void testShouldBeAbleToFindEncapsulatedFixtureName() throws Exception {
    String fitPage = "<table>" + geDecoratorHTMLRow() + "<tr><td>eg.Division</td></tr></table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    String encapsulatedFixtureName = (String) dispatcher.summary.get(FixtureDecorator.ENCAPSULATED_FIXTURE_NAME);
    assertEquals("eg.Division", encapsulatedFixtureName);
  }

  public void testShouldBeAbleToInstantiateEncapsulatedFixture() throws Exception {
    String fitPage = "<table>" + geDecoratorHTMLRow() + "<tr><td>" + TestFixture.class.getName()
      + "</td></tr></table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    String encapsulatedFixtureName = (String) dispatcher.summary.get(FixtureDecorator.ENCAPSULATED_FIXTURE_NAME);
    assertEquals("fit.decorator.TestFixture", encapsulatedFixtureName);
  }

  public void testShouldDoNothingIfThereIsNoEncapsulatedFixturePresent() throws Exception {
    String fitPage = "<table>" + geDecoratorHTMLRow() + "</table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    assertNull(dispatcher.summary.get(FixtureDecorator.ENCAPSULATED_FIXTURE_NAME));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(0, 0, 0, 0), dispatcher.counts);
  }

  public void testShouldMarkExceptionIfEncapsulatingFixtureNameIsInvalid() throws Exception {
    String fitPage = "<table>" + geDecoratorHTMLRow() + "<tr><td>invalidClass</td></tr></table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    assertEquals(1, dispatcher.counts.exceptions);
    String encapsulatedFixtureName = (String) dispatcher.summary.get(FixtureDecorator.ENCAPSULATED_FIXTURE_NAME);
    assertEquals("invalidClass", encapsulatedFixtureName);
  }

  public void testShouldStripFirstRowAndPassRestOfTheTableToEncapsulatedFixture() throws Exception {
    String fitPage = "<table>" + geDecoratorHTMLRow() + "<tr><td>" + TestFixture.class.getName()
      + "</td></tr></table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    String expectedTableContents = "<table><tr><td>" + TestFixture.class.getName() + "</td></tr></table>";
    assertEquals(expectedTableContents, dispatcher.summary.get(TestFixture.TABLE_CONTENTS));
  }

  public void testShouldHandleInvalidInputExceptionIfThrownBySetUpMethod() throws Exception {
    String fitPage = "<table>" + geWrongDecoratorHTMLRow() + "<tr><td>" + TestFixture.class.getName()
      + "</td></tr></table>";
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(0, 0, 0, 1), dispatcher.counts);
  }

  public void testSetAlternativeArgsShouldStoreOddNumberedColumnsToArgsVariable() throws Exception {
    String fitPage = "<table><tr><td>xyz</td><td>1</td><td>skip1</td><td>2</td><td>skip2</td>"
      + "<td>3</td><td>skip3</td></tr></table>";
    FixtureDecorator decorator = dummyFitDecorator();
    Parse table = new Parse(fitPage);
    decorator.setAlternativeArgs(table);
    assertArray(new String[]
      {"1", "2", "3"}, decorator.getArgs());
  }

  public void testSetAlternativeArgsShouldIgnoreExpectedAndActualStrings() throws Exception {
    String fitPage = "<table><tr><td>xyz</td><td>1</td><td>skip1</td><td>2<hr>actual 4</td><td>skip2</td>"
      + "<td>3</td><td>skip3</td></tr></table>";
    FixtureDecorator decorator = dummyFitDecorator();
    Parse table = new Parse(fitPage);
    decorator.setAlternativeArgs(table);
    assertArray(new String[]
      {"1", "2", "3"}, decorator.getArgs());
  }

  private FixtureDecorator dummyFitDecorator() {
    FixtureDecorator decorator = new FixtureDecorator() {
      protected void setupDecorator(String[] args) throws InvalidInputException {
      }

      protected void updateColumnsBasedOnResults(Parse table) {
      }
    };
    return decorator;
  }

  private void assertArray(String[] expected, String[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < actual.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  private String geWrongDecoratorHTMLRow() {
    return "<tr><td>" + Loop.class.getName() + "</td></tr>";
  }

  protected abstract String geDecoratorHTMLRow();

  protected abstract int numberOfAssertionsOnDecorator();

  protected void executeAndAssert(Counts expected, String fitPage, Fixture fixture) throws ParseException {
    fixture.doTable(new Parse(fitPage));
    TestCaseHelper.assertCounts(expected, fixture.counts);
  }
}
