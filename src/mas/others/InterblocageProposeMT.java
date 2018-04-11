package mas.others;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate.MatchExpression;

public class InterblocageProposeMT implements MatchExpression{
	private static final long serialVersionUID = 8688081240099240575L;
	
	
	public InterblocageProposeMT() {
		super();
	}
	
	@Override
	public boolean match(ACLMessage msg) {
		// TODO Auto-generated method stub
		
		if(msg.getPerformative() == ACLMessage.PROPOSE && msg.getContent().contains("INTERBLOCAGE")) {
			return true;
		}
		return false;
	}

}
