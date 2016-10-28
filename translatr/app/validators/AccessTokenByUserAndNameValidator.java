package validators;

import javax.validation.ConstraintValidator;

import models.AccessToken;
import models.User;
import play.data.validation.Constraints;
import play.libs.F.Tuple;

/**
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
public class AccessTokenByUserAndNameValidator extends Constraints.Validator<Object>
			implements ConstraintValidator<AccessTokenByUserAndName, Object>
{
	public static final String MESSAGE = "error.accesstokenbyuserandname";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(AccessTokenByUserAndName constraintAnnotation)
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

		return AccessToken.byUserAndName(User.loggedInUserId(), (String)object) == null;
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
