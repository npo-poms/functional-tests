package nl.specialisterren.fitnesse.fixture.slim;

public class IntegerFixture {
	public boolean valueIsLessThan(int a, int b) {
		return a < b;
	}

	public int addAnd(String a, int b) {
        Integer x = null;
        if (a != null) {
            x = Integer.valueOf(a);
        }
		// TODO: this may produce NullPointerException:
        return x + b;
    }

    public int subtractAnd(String a, int b) {
        Integer x = null;
        if (a != null) {
            x = Integer.valueOf(a);
        }

		// TODO: this may produce NullPointerException:
        return x - b;
    }
}
