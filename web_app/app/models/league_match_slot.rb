class LeagueMatchSlot < MatchSlot
  validates_presence_of :matchday_slot
  belongs_to :matchday_slot

  undef :contestant
  undef :client=
  undef :client

  delegate :contestant, :client, :to => :matchday_slot

  def occupied?
    !!contestant
  end
end