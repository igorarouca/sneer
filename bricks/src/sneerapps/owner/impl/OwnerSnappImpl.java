package sneerapps.owner.impl;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSeparator;

import sneer.bricks.ownAvatar.OwnAvatarKeeper;
import sneer.bricks.ownName.OwnNameKeeper;
import sneer.bricks.ownTagline.OwnTaglineKeeper;
import sneer.lego.Inject;
import sneer.skin.imageSelector.ImageSelector;
import sneer.widgets.reactive.ImageWidget;
import sneer.widgets.reactive.RFactory;
import sneer.widgets.reactive.TextWidget;
import sneerapps.owner.OwnerSnapp;

public class OwnerSnappImpl implements OwnerSnapp {

	@Inject
	static private OwnNameKeeper _ownNameKeeper;

	@Inject
	static private OwnTaglineKeeper _ownTaglineKeeper;

	@Inject
	static private OwnAvatarKeeper _ownAvatarKeeper;

	@Inject
	static private ImageSelector _imageSelector;

	@Inject
	static private RFactory rfactory;

	private TextWidget editableLabel;

	@Override
	public void init(Container container) {	
		container.setLayout(new GridBagLayout());
		
		GridBagConstraints c;
		
		c = getConstraints(0, 10,10,0,10);
		editableLabel = rfactory.newEditableLabel(
	        	_ownNameKeeper.name(), 
				_ownNameKeeper.nameSetter());
		
        container.add(editableLabel.getContainer(), c);
 
		c = getConstraints(1, 0,10,0,0);
        JSeparator separator = new JSeparator();
		container.add(separator, c);
        
		c = getConstraints(2, 0,10,10,10);
        editableLabel = rfactory.newEditableLabel(
        		_ownTaglineKeeper.tagline(), 
        		_ownTaglineKeeper.taglineSetter());
        container.add(editableLabel.getContainer(), c);
        
		c = new GridBagConstraints(1,0, 1,3,0.0,0.0,
				GridBagConstraints.EAST, 
				GridBagConstraints.BOTH,
				new Insets(5,0,5,5),0,0);
		
		ImageWidget avatar = rfactory.newImage(_ownAvatarKeeper.avatar(32));
		container.add(avatar.getComponent(), c);
		
		avatar.getMainWidget().addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				_imageSelector.open(_ownAvatarKeeper.avatarSetter());
			}});
	}

	private GridBagConstraints getConstraints(int y, int top, int left, int botton, int right) {
		GridBagConstraints c;
		c = new GridBagConstraints(0,y,1,1,1.0,1.0,
					GridBagConstraints.EAST, 
					GridBagConstraints.HORIZONTAL,
					new Insets(top,left,botton,right),0,0);
		return c;
	}

	@Override
	public String getName() {
		return "Owner";
	}
}