module PeopleHelper
  def role_for(person, contestant)
    person.membership_for(contestant)
  end
end
