class MatchdaySlot < ActiveRecord::Base
  validates_presence_of :contestant
  validates_presence_of :matchday

  belongs_to :matchday
  belongs_to :contestant
  belongs_to :client
  belongs_to :score, :dependent => :destroy

  has_one :match_slot, :dependent => :destroy
  has_many :matches, :through => :match_slot

  acts_as_list :scope => :matchday_id
end