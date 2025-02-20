package br.com.nlw.events.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;

public interface SubscriptionRepo extends CrudRepository<Subscription, Integer>{

	public Subscription findByEventAndSubscriber(Event event, User subscriber);
	
	@Query(value = " SELECT                                            "
		         + "    COUNT(s.subscription_number) AS quantidade,    "
		         + "    s.indication_user_id,                          "
		         + "    u.user_name                                    "
		         + " FROM                                              "
		         + "    tbl_subscription s                             "
		         + "        INNER JOIN                                 "
		         + "    tbl_user u ON u.user_id = s.indication_user_id "
		         + " WHERE                                             "
		         + "    s.indication_user_id IS NOT NULL               "
		         + "        AND s.event_id = :event_id                 "
		         + " GROUP BY s.indication_user_id                     "
		         + " ORDER BY quantidade DESC                          ",
		 nativeQuery = true)	
	public List<SubscriptionRankingItem> generateRanking(@Param("event_id") Integer event_id);
}
