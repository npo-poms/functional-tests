package nl.specialisterren.fitnesse.fixture.slim;

public class BooleanFixture {
	public BooleanFixture() {
	}
	
	public boolean checkCanDeleteIfFieldIsNotEmptyAndCannotDeleteIfFieldIsEmpty(boolean isFieldEmpty, boolean canDelete) {
		if (!isFieldEmpty)
			return canDelete;
		
		return !canDelete;
	}
}
