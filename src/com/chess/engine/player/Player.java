package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Player {
    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    public Player(final Board board, final Collection<Move> legalMoves,
                  final Collection<Move> opponentMoves) {
        this.board = board;
        this.playerKing = establishKing();
        this.legalMoves = ImmutableList.copyOf(
                Iterables.concat(legalMoves, calculateKingCastles(legalMoves, opponentMoves)));
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(),
                opponentMoves).isEmpty();

    }

    protected static Collection<Move> calculateAttacksOnTile(
            int piecePosition, Collection<Move> opponentMoves) {
        final List<Move> attackMoves = new ArrayList<>();
        for (final Move move : opponentMoves) {
            if(piecePosition == move.getDestinationCoordinate()) {
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            //
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("Should not reach here! Not a valid board!");
    }

    public boolean isMoveLegal(final Move move) {
        return this.legalMoves.contains(move);
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isInCheckmate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStalemate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    //TODO- more work here
    public boolean isCastled() {
        return false;
    }

    public MoveTransition makeMove(final Move move) {
       if (!this.isMoveLegal(move)) {
           return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
       }

       final Board transitionBoard = move.execute();

       final Collection<Move> kingAttacks = Player.calculateAttacksOnTile(
               transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(),
               transitionBoard.currentPlayer().getLegalMoves());
       if (!kingAttacks.isEmpty()) {
           return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
       }

        return new MoveTransition(transitionBoard, move, MoveStatus.DONE);
    }

    public Collection<Move> getLegalMoves() {
        return this.legalMoves;
    }

    public Piece getPlayerKing() {
        return this.playerKing;
    }

    protected boolean hasEscapeMoves() {
        for (final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move);
            if(transition.getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    public abstract Collection<Piece> getActivePieces();

    public abstract Alliance getAlliance();

    public abstract Player getOpponent();

    protected abstract Collection<Move>
    calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentLegals);
}
