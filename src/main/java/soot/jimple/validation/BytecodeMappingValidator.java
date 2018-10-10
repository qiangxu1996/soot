package soot.jimple.validation;

import soot.Body;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.internal.AbstractStmt;
import soot.validation.BodyValidator;
import soot.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;

public enum BytecodeMappingValidator implements BodyValidator {

	INSTANCE;

	public static BytecodeMappingValidator v() {
		return INSTANCE;
	}

	/**
	 * The expected offset sequence is -1, -1, ..., -1, 0, 1, ..., N, -1, -1, ..., -1.
	 */
	@Override
	public void validate(Body body, List<ValidationException> exceptions) {
		long prevOffset;
		long currOffset = -1;
		long insnNum = ((JimpleBody) body).getBytecodeInsnNum();
		boolean endOfMethod = false;

		for (Unit unit : body.getUnits()) {
			prevOffset = currOffset;
			currOffset = ((AbstractStmt) unit).getOffsetInBytecode();

			if (!endOfMethod) {
				long offsetDiff = currOffset - prevOffset;
				if (offsetDiff != 1 && offsetDiff != 0)
					/* the case of this and params are handled in the if condition above */
					if (currOffset == -1 && prevOffset + 1 == insnNum)
						endOfMethod = true;
					else
						exceptions.add(new ValidationException(unit, "Offset difference greater than 1"));
			} else {
				if (currOffset != -1)
					exceptions.add(new ValidationException(unit, "Offset other than -1 at the end of method"));
			}
		}

		if (!endOfMethod && currOffset + 1 != insnNum)
			exceptions.add(new ValidationException(body.getUnits().getLast(), "No mapping at the end of method"));
	}

	public void validate(Body body) {
		List<ValidationException> exceptionList = new ArrayList<>();
		validate(body, exceptionList);
		if (!exceptionList.isEmpty())
			throw exceptionList.get(0);
	}

	@Override
	public boolean isBasicValidator() {
		return false;
	}
}
