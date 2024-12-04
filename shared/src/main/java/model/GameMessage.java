package model;

import chess.ChessGame;

public record GameMessage(int gameID, ChessGame.TeamColor color, String boardString) {
}
