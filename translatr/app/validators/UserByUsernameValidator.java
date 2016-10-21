package validators;

import javax.validation.ConstraintValidator;

import models.User;
import play.data.validation.Constraints;
import play.libs.F.Tuple;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
public class UserByUsernameValidator extends Constraints.Validator<Object>
			implements ConstraintValidator<UserByUsername, Object>
{
	public static final String MESSAGE = "error.userbyusername";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(UserByUsername constraintAnnotation)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(Object object)
	{
		if(object == null)
			return false;

		if(!(object instanceof String))
			return false;

		return User.byUsername((String)object) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tuple<String, Object[]> getErrorMessageKey()
	{
		return null;
	}

}
