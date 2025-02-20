package br.com.nlw.events.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIdIndicadorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repo.EventRepo;
import br.com.nlw.events.repo.SubscriptionRepo;
import br.com.nlw.events.repo.UserRepo;

@Service
public class SubscriptionService {

	@Autowired
	EventRepo evtRepo;
	
	@Autowired
	UserRepo userRepo;
	
	@Autowired
	SubscriptionRepo subscriptionRepo;
	

    public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId){
        Event evt = evtRepo.findByPrettyName(eventName);
        if (evt == null){
            throw new EventNotFoundException("Evento " + eventName+ " não existe");
        }
        User userRec = userRepo.findByEmail(user.getEmail());
        if(userRec == null){
            userRec = userRepo.save(user);
        }

        User indicador = null;
        
		if (userId != null) {
			indicador = userRepo.findById(userId).orElse(null);
			if (indicador == null) {
				throw new UserIdIndicadorNotFoundException("Usuário " +userId+ " indicador não existe");
			}
		}

		Subscription subs = new Subscription();
		subs.setEvent(evt);
		subs.setSubscriber(userRec);
		subs.setIndication(indicador);

        Subscription tmpSub = subscriptionRepo.findByEventAndSubscriber(evt, userRec);
        if(tmpSub != null){
            throw new SubscriptionConflictException("Já existe incrição para o usuário " + userRec.getName() + " no evento " + evt.getTitle());
        }

        Subscription res = subscriptionRepo.save(subs);
        return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecraft.com/subscription/"+res.getEvent().getPrettyName()+"/"+res.getSubscriber().getId());
    }
    
    public List<SubscriptionRankingItem> getCompleteRanking(String prettyName) {
    	Event evt = evtRepo.findByPrettyName(prettyName);
    	if (evt == null) {
    		throw new EventNotFoundException("Ranking do evento "+ prettyName + " não existe!");
		}
    	return subscriptionRepo.generateRanking(evt.getEventId());
	}
    
    public SubscriptionRankingByUser getRankingByUser(String prittyName ,Integer userId) {
    	List<SubscriptionRankingItem> ranking = getCompleteRanking(prittyName);
    	SubscriptionRankingItem item = ranking.stream().filter(i -> i.userId().equals(userId)).findFirst().orElse(null);
    	if (item == null) {
			throw new UserIdIndicadorNotFoundException("Não a inscrições para este usuário "+userId+"!");
		}
    	
    	Integer posicao = IntStream.range(0, ranking.size()).filter(pos -> ranking.get(pos).userId().equals(userId)).findFirst().getAsInt();
    	System.out.println(item);
    	return new SubscriptionRankingByUser(item, posicao+1);
	}
}
