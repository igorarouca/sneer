package sneer.bricks.snapps.games.go.impl.gui.game;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.snapps.games.go.impl.TimerFactory;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.BoardImagePainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.BoardPainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.DarkBorderPainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.HUDPainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.HoverStonePainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.StonePainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.StonesInPlayPainter;
import sneer.bricks.snapps.games.go.impl.logging.GoLogger;
import sneer.bricks.snapps.games.go.impl.logic.BoardListener;
import sneer.bricks.snapps.games.go.impl.logic.GoBoard;
import sneer.bricks.snapps.games.go.impl.logic.GoBoard.StoneColor;
import basis.environments.ProxyInEnvironment;

public class GoBoardPanel extends JPanel{


	private static final long serialVersionUID = 1L;

	private static final float CELL_MAX_SIZE = 100;
	private static final float CELL_MIN_SIZE = 5;

	private static final int SCROLL_EDGE = 60;
	
	private int _boardImageSize;
	private float _cellSize;
	private final GoBoard _board;

	private BufferedImage _bufferImage;

	private final StoneColor _side;

	private BoardPainter _boardPainter;
	private HoverStonePainter _hoverStonePainter;
	private StonesInPlayPainter _stonesInPlayPainter;
	private HUDPainter _hudPainter;
	
	@SuppressWarnings("unused")
	private WeakContract _referenceToAvoidGc;

	private final GuiPlayer _goFrame;

	private StonePainter _stonePainter;

	private boolean _hasResigned = false;
	private boolean _otherResigned = false;
	
	private int scrollX = 0;
	private int scrollY = 0;

	private DarkBorderPainter _darkBorderPainter;

	private final GameMenu _gameMenu;

	private BoardImagePainter _boardImagePainter;

	private Offset _offset;
	
	public GoBoardPanel(final GuiPlayer goFrame,GoBoard goBoard,final TimerFactory timerFactory,BoardImagePainter boardImagePainter,StoneColor side, Offset offset) {
		_goFrame = goFrame;
		_offset = offset;
		_gameMenu = new GameMenu(this);
		final ActionListener onPassPress = new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			GoLogger.log("doMovePass();");
			goFrame.doMovePass();
		}};
		
		final ActionListener onResignPress = new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			GoLogger.log("doMoveResign();");
			goFrame.doMoveResign();
		}};
		
		_gameMenu._passButton.addActionListener(onPassPress);
		_gameMenu._resignButton.addActionListener(onResignPress);
		
		_side = side;
		_board = goBoard;
		
		_boardImagePainter = boardImagePainter;
		
		updateTurnMessage();
		
		createPainters();
		
		addMouseListener();
		
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			
        	private static final int SCROLL_KEYBOARD_SPEED = 10;
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if(e.isControlDown()){
					if(e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS ){
						updateCellSize(1);
					}
					if(e.getKeyCode() == KeyEvent.VK_MINUS){
						updateCellSize(-1);
					}
				}
				if(e.getKeyCode() == KeyEvent.VK_UP){
					_offset.increaseYOffset(-SCROLL_KEYBOARD_SPEED);
				}
				if(e.getKeyCode() == KeyEvent.VK_DOWN){
					_offset.increaseYOffset(SCROLL_KEYBOARD_SPEED);
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT){
					_offset.increaseXOffset(-SCROLL_KEYBOARD_SPEED);
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					_offset.increaseXOffset(SCROLL_KEYBOARD_SPEED);
				}
				return false;
			}
		});
		
		_referenceToAvoidGc = timerFactory.wakeUpEvery(30, new Runnable() {@Override public void run() {
			update();
			repaint();
		}});    	
	}
	
	public void update(){
		_offset.increaseXOffset(scrollX);
		_offset.increaseYOffset(scrollY);
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		render(g2);
		paintComponents(g2);
		g.dispose();
	}
	
	public void render(final Graphics2D graphics) {
		Graphics2D buffer = getBuffer();
		_boardPainter.draw(buffer);
		_stonesInPlayPainter.draw(buffer, _board);
		_hoverStonePainter.draw(buffer, _board);
		drawBoardImage(graphics);
		_darkBorderPainter.draw(graphics);
		_hudPainter.draw(graphics);
	}

	public int scoreWhite() {
		return _board.whiteScore();
	}

	public int scoreBlack() {
		return _board.blackScore();
	}

	public void setBoardListener(BoardListener boardListener) {
		_board.setBoardListener(boardListener);
	}

	public float getCellSize() {
		return _cellSize;
	}

	public void setLostByReign() {
		_hasResigned = true;
	}

	void receiveMoveAddStone(int xCoordinate, int yCoordinate) {
		GoLogger.log("GoBoardPanel.receiveMoveAddStone("+xCoordinate+","+yCoordinate+")");
		_board.playStone(xCoordinate, yCoordinate);
		updateTurnMessage();
		decideWinner();
	}

	private void updateTurnMessage() {
		_gameMenu.setMessage("Your turn.");
		updateTurnMessageIfWaiting();
	}

	private void updateTurnMessageIfWaiting() {
		if(_board.nextToPlay() != _side){
			_gameMenu.setMessage("Waiting for the other player...");
		}
		if(_board.nextToPlay() == null){
			_gameMenu.setMessage("Game ended. Mark the dead stones to decide the winner.");
		}
	}

	void receiveMoveMarkStone(int xCoordinate, int yCoordinate) {
		GoLogger.log("GoBoardPanel.receiveMoveMarkStone("+xCoordinate+","+yCoordinate+")");
		_board.toggleDeadStone(xCoordinate, yCoordinate);
		decideWinner();
	}

	void receiveMovePassTurn() {
		GoLogger.log("GoBoardPanel.receiveMovePassTurn()");
		_board.passTurn();
		_gameMenu.setMessage("Other player passed. Your turn.");
		updateTurnMessageIfWaiting();
		decideWinner();
	}

	void receiveMoveResign() {
		if(!_hasResigned)
			_otherResigned = true;
		GoLogger.log("GoBoardPanel.receiveMoveResign()");
		_board.resign();
		if(_hasResigned){
			_gameMenu.setMessage("You lost by resign.");
		}else{
			_gameMenu.setMessage("Other player resigned. You Won!");
		}
		_gameMenu.setGameEnded();
	}

	private void createPainters() {
		_cellSize = 60;
		int boardSize = _board.size();
		setBoardSize(boardSize);
		_offset.setXOffset((int) (_cellSize - _boardImageSize));
		_offset.setYOffset((int) (_cellSize - _boardImageSize));
		
		_boardPainter = new BoardPainter(boardSize, _boardImageSize, _cellSize);
		_stonePainter = new StonePainter(_boardImageSize, _cellSize);
		_hoverStonePainter = new HoverStonePainter(_stonePainter,boardSize, _cellSize);		
		_stonesInPlayPainter = new StonesInPlayPainter(_stonePainter,_cellSize);
		_darkBorderPainter = new DarkBorderPainter(SCROLL_EDGE, _gameMenu.getMenuWidth());
		_hudPainter = new HUDPainter();
	}

	private void setBoardSize(int boardSize) {
		_boardImageSize = (int) (_cellSize*(boardSize));
		_offset.setBoardImageSize(_boardImageSize);
	}
	
	private void updateCellSize(int add) {
		float newCellSize = _cellSize + add;
		if(newCellSize > CELL_MAX_SIZE){
			newCellSize = CELL_MAX_SIZE;
		}
		if(newCellSize < CELL_MIN_SIZE){
			newCellSize = CELL_MIN_SIZE;
		}
		
		float oldBoardImageSize = _boardImageSize;
		
		_cellSize = newCellSize;
		int boardSize = _board.size();
		setBoardSize(boardSize);
		
		_offset.setXOffset((int) ((_offset.getXOffset() / oldBoardImageSize)*_boardImageSize));
		_offset.setYOffset((int) ((_offset.getYOffset() / oldBoardImageSize)*_boardImageSize));
		
		_boardPainter.setBoardDimensions(boardSize, _boardImageSize, _cellSize);
		_stonePainter.setBoardDimensions(_boardImageSize, _cellSize);
		_hoverStonePainter.setBoardDimensions(boardSize, _cellSize);
		_stonesInPlayPainter.setBoardDimensions(_cellSize);
	}
	private void doMoveAddStone(int x, int y) {
		GoLogger.log("GoBoardPanel.doMoveAddStone("+x+","+y+")");
		_goFrame.doMoveAddStone(x,y);
		decideWinner();
	}
	
	private void doMoveMarkStone(int x, int y) {
		GoLogger.log("GoBoardPanel.doMoveMarkStone("+x+","+y+")");
		_goFrame.doMoveMarkStone(x,y);
		decideWinner();
	}
	
	private void addMouseListener() {
		Object listener = ProxyInEnvironment.newInstance(new GoMouseListener());
		addMouseListener((MouseListener) listener);
	    addMouseMotionListener((MouseMotionListener) listener);
	    addMouseWheelListener((MouseWheelListener) listener);
	}
	
	private void decideWinner() {
		int winState = HUDPainter.NOONE_WIN;
		if (_board.nextToPlay()==null){
			_goFrame.gameEnded();
			int scoreWhite=scoreWhite();
			int scoreBlack=scoreBlack();
			boolean isNotADraw = scoreWhite!=scoreBlack;
			if (isNotADraw){
				boolean isWinner=false;
				if (_side==StoneColor.WHITE){
					isWinner=(scoreWhite>scoreBlack);
				}else{
					isWinner=(scoreWhite<scoreBlack);
				}
				if(isWinner){
					winState = HUDPainter.PLAYER_WIN;
				}else{
					winState = HUDPainter.PLAYER_LOSES;
				}
			}
		}
		
		if(_hasResigned){
			winState = HUDPainter.PLAYER_LOSES;
		}
		if(_otherResigned){
			winState = HUDPainter.PLAYER_WIN;
		}
		
		_hudPainter.setWinState(winState);
		
	}

	private void drawBoardImage(Graphics graphics) {
		Rectangle boardImageRectangle = new Rectangle(_offset.getXOffset(), _offset.getYOffset(), _boardImageSize, _boardImageSize);
		final BufferedImage bufferImage = _bufferImage;
		_boardImagePainter.drawBoardAndSurroundings(graphics, boardImageRectangle, bufferImage);
	}

	private Graphics2D getBuffer() {
		_bufferImage = new BufferedImage((int)(_boardImageSize+_cellSize), (int)(_boardImageSize+_cellSize), BufferedImage.TYPE_INT_ARGB);
		return (Graphics2D)_bufferImage.getGraphics();
	}

	
	private int toScreenPosition(final int coordinate) {
		int coordinateInsideBoard = coordinate %  _boardImageSize;
		float result = (coordinateInsideBoard -  (_cellSize / 2)) / _cellSize;
		return (int)Math.ceil(result)%_board.size();
	}
	
	private class GoMouseListener extends MouseAdapter {
		private static final int SCROLL_SPEED = 5;
		private int _startX;
		private int _startY;

		@Override 
		public void mouseMoved(final MouseEvent e) {
			final int mouseX = e.getX();
			final int mouseY = e.getY();
			
			_hoverStonePainter.setHoverX(toScreenPosition(mouseX-_offset.getXOffset()));
			_hoverStonePainter.setHoverY(toScreenPosition(mouseY-_offset.getYOffset()));
			
			scrollIfOnScrollRegion(mouseX, mouseY);
			
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			stopScrolling();
		}

		private void stopScrolling() {
			scrollX = 0;
			scrollY = 0;
		}
		
		private void scrollIfOnScrollRegion(final int mouseX, final int mouseY) {
			scrollHorizontallyIfOnScrollRegion(mouseX);
			scrollVerticallyIfOnScrollRegion(mouseX,mouseY);
		}

		private void scrollHorizontallyIfOnScrollRegion(final int mouseX) {
			if(mouseX < SCROLL_EDGE){
				scrollX = SCROLL_SPEED;
				return;
			}
			if(mouseX > getWidth()-SCROLL_EDGE-_gameMenu.getMenuWidth()){
				if(mouseX < getWidth()-_gameMenu.getMenuWidth()){
					scrollX = -SCROLL_SPEED;
					return;
				}
			}
			scrollX = 0;
		}
		
		private void scrollVerticallyIfOnScrollRegion(final int mouseX,final int mouseY) {
			if(mouseY < SCROLL_EDGE){
				if(mouseX < getWidth()-_gameMenu.getMenuWidth()){
					scrollY = SCROLL_SPEED;
					return;
				}
			}
			if(mouseY > getHeight()-SCROLL_EDGE){
				if(mouseX < getWidth()-_gameMenu.getMenuWidth()){
					scrollY = -SCROLL_SPEED;
					return;
				}
			}
			scrollY = 0;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			final int mouseX = e.getX();
			final int mouseY = e.getY();
			
			if(!dragActionButtonsPressed(e))return;
			
			_offset.increaseXOffset(mouseX - _startX);
			_offset.increaseYOffset(mouseY - _startY);
			
			_startX = mouseX;
			_startY = mouseY;
			repaint();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(dragActionButtonsPressed(e)){
				_startX = e.getX();
				_startY = e.getY();
			}
		}

		private boolean dragActionButtonsPressed(MouseEvent e) {
			boolean middleMouseButton = SwingUtilities.isMiddleMouseButton(e);
			boolean ctrlClick =  e.isControlDown() && SwingUtilities.isLeftMouseButton(e);
			return middleMouseButton || ctrlClick;
		}
		
		@Override 
		public void mouseReleased(MouseEvent e) {
			if(dragActionButtonsPressed(e)) return;
			
			int x = toScreenPosition(e.getX()-_offset.getXOffset());
			int y = toScreenPosition(e.getY()-_offset.getYOffset());
			if (_board.nextToPlay()==null) {
				doMoveMarkStone(x, y);
				return;
			}
			if (!_board.canPlayStone(x, y)) return;
			if (_side != _board.nextToPlay()) return;
			doMoveAddStone(x, y);
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			final int wheelRotation = e.getWheelRotation();
			int factor = 2;
			updateCellSize(wheelRotation*factor*-1);
		}
	}

	public void updateScore(int blackScore, int whiteScore) {
		_gameMenu.updateScore(blackScore, whiteScore);
	}

	public void nextToPlay(StoneColor nextToPlay) {
		boolean isMyTurn = nextToPlay == _side;
		_gameMenu.setMyTurn(isMyTurn);
		repaint();
	}

	public void setGameEnded() {
		_gameMenu.setGameEnded();
	}

}
