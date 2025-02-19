package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;
import static com.chess.engine.board.Move.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece {
    private final static int[] CANDIDATE_MOVE_VECTOR_CANDIDATES = {7, 8, 9, 16};
    public Pawn(int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, true);
    }

    public Pawn(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int currentCandidateOffset: CANDIDATE_MOVE_VECTOR_CANDIDATES) {
            final int candidateDestinationCoordinate = this.piecePosition + (currentCandidateOffset *
                    this.getPieceAlliance().getDirection());
            if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }

            if(currentCandidateOffset == 8 &&
                    !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                    addPawnPromotions(board, candidateDestinationCoordinate,
                            null, legalMoves);
                } else {
                    legalMoves.add(new Move.PawnMove
                            (board, this, candidateDestinationCoordinate));
                }
            } else if (currentCandidateOffset == 16 && this.isFirstMove() &&
                    ((BoardUtils.SEVENTH_RANK[this.piecePosition] && this.pieceAlliance.isBlack()) ||
                    (BoardUtils.SECOND_RANK[this.piecePosition] && this.pieceAlliance.isWhite()))) {
                final int behindCandidateDestinationCoordinate = this.piecePosition +
                        (this.pieceAlliance.getDirection() * 8);
                if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    legalMoves.add(new Move.PawnJump(board,
                            this, candidateDestinationCoordinate));
                }
            } else if (currentCandidateOffset == 7 &&
                    !((BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite() ||
                    (BoardUtils.FIRST_COLUMN[this.piecePosition]) && this.pieceAlliance.isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnTile = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnTile.pieceAlliance) {
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            addPawnPromotions(board, candidateDestinationCoordinate,
                                    pieceOnTile, legalMoves);
                        } else {
                            legalMoves.add(new PawnAttackMove(board,
                                    this, candidateDestinationCoordinate, pieceOnTile));
                        }
                    }
                } else if (board.getEnPassantPawn() != null) {
                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition +
                        this.pieceAlliance.getOppositeDirection())) {
                        Piece pieceOnCandidate = board.getEnPassantPawn();
                        if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                            legalMoves.add(new PawnEnPassantAttackMove(board,
                                    this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }
            } else if (currentCandidateOffset == 9 &&
                    !((BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite() ||
                    BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnTile = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnTile.pieceAlliance) {
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            addPawnPromotions(board, candidateDestinationCoordinate,
                                    pieceOnTile, legalMoves);
                        } else {
                            legalMoves.add(new PawnAttackMove(board,
                                    this, candidateDestinationCoordinate, pieceOnTile));
                        }
                    }
                } else if (board.getEnPassantPawn() != null) {
                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition -
                        this.pieceAlliance.getOppositeDirection())) {
                    final Piece pieceOnCandidate = board.getEnPassantPawn();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        //TODO work with promotion here
                        legalMoves.add(new PawnEnPassantAttackMove(board,
                                this, candidateDestinationCoordinate, pieceOnCandidate));
                    }
                }
            }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }

    @Override
    public Pawn movePiece(Move move) {
        return new Pawn(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance());
    }

    private void addPawnPromotions(final Board board,
                                   int candidateDestinationCoordinate,
                                   final Piece pieceOnTile,
                                   Collection<Move> legalMoves) {
        if (pieceOnTile == null) {
            legalMoves.add(new PawnPromotion(new PawnMove(board, this,
                    candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen
                    (this.pieceAlliance, candidateDestinationCoordinate)));
            legalMoves.add(new PawnPromotion(new PawnMove(board, this,
                    candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook
                    (this.pieceAlliance, candidateDestinationCoordinate)));
            legalMoves.add(new PawnPromotion(new PawnMove(board, this,
                    candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight
                    (this.pieceAlliance, candidateDestinationCoordinate)));
            legalMoves.add(new PawnPromotion(new PawnMove(board, this,
                    candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop
                    (this.pieceAlliance, candidateDestinationCoordinate)));
        } else {
            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this,
                    candidateDestinationCoordinate, pieceOnTile), PieceUtils.INSTANCE.getMovedQueen
                    (this.pieceAlliance, candidateDestinationCoordinate)));
            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this,
                    candidateDestinationCoordinate, pieceOnTile), PieceUtils.INSTANCE.getMovedRook
                    (this.pieceAlliance, candidateDestinationCoordinate)));
            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this,
                    candidateDestinationCoordinate, pieceOnTile), PieceUtils.INSTANCE.getMovedKnight
                    (this.pieceAlliance, candidateDestinationCoordinate)));
            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this,
                    candidateDestinationCoordinate, pieceOnTile), PieceUtils.INSTANCE.getMovedBishop
                    (this.pieceAlliance, candidateDestinationCoordinate)));
        }
    }
}
