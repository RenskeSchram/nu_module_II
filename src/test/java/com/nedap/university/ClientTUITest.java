    package com.nedap.university;

    import static org.junit.jupiter.api.Assertions.assertFalse;

    import java.io.ByteArrayInputStream;
    import java.io.IOException;
    import org.junit.jupiter.api.AfterEach;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;

public class ClientTUITest {

  private ClientTUI clientTUI;

  @BeforeEach
  protected void setUp() {
    clientTUI = new ClientTUI();
  }

  @AfterEach
  public void reset() {
    System.setIn(System.in);
  }

  @Test
  public void testCorrectPORT() throws IOException {

    // correctly start and exit tui
    String input = "172.16.0.0\n1234\ndisconnect\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    try {
      clientTUI.runTUI();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      System.setIn(System.in);
    }
    assertFalse(clientTUI.runTui);
  }

  @Test
  public void testWrongPORT() throws IOException {
    String input = "172.16.0.0\n909090\n9090\ndisconnect";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    try {
      clientTUI.runTUI();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      System.setIn(System.in);
    }

    assertFalse(clientTUI.runTui);
  }

}


