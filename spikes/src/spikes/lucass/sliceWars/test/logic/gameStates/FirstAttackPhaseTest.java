package spikes.lucass.sliceWars.test.logic.gameStates;

import static org.junit.Assert.assertEquals;

import java.awt.Polygon;

import org.junit.Test;

import spikes.lucass.sliceWars.src.logic.BoardCell;
import spikes.lucass.sliceWars.src.logic.BoardCellImpl;
import spikes.lucass.sliceWars.src.logic.Player;
import spikes.lucass.sliceWars.src.logic.gameStates.FirstAttacks;
import spikes.lucass.sliceWars.src.logic.gameStates.GameState;
import spikes.lucass.sliceWars.src.logic.gameStates.GameStateContext.Phase;

public class FirstAttackPhaseTest {

	@Test
	public void testState(){
		final BoardCellImpl boardCell = new BoardCellImpl(new Polygon());
		boardCell.setOwner(Player.PLAYER1);
		boardCell.setDiceCount(1);
		final int boardCellCount = 1;
		FirstAttacks subject = new  FirstAttacks(new Player(1, 2),new BoardMockAdapter() {
			
			@Override
			public BoardCell getCellAtOrNull(int x, int y) {
				return boardCell;
			}

			@Override
			public int getCellCount() {
				return boardCellCount;
			}
		});
		GameState nextPhase = subject.play(0, 0);
		assertEquals(Phase.FIRST_ATTACKS,nextPhase.getPhase());
		subject.pass();
		nextPhase = subject.play(0, 0);
		assertEquals(Phase.FIRST_ATTACKS,nextPhase.getPhase());
		GameState afterPass = subject.pass();
		assertEquals(Phase.DICE_DISTRIBUTION,afterPass.getPhase());
	}
	
}
