package br.ufal.ic.JCAM;

import java.util.Arrays;
import java.util.List;

public class Moabson {

	private LexicalAnalyzer lexicalAnalyzer;
	private Token currentToken;

	// se houver um ou mais erros, mudar para false
	private boolean success = true;

	private static List<TokenCategory> typesCategory;
	// private static List<TokenCategory> commandsWithScope;
	// private static List<TokenCategory> commandsWithoutScope;

	static {
		typesCategory = Arrays.asList(TokenCategory.PR_TIPO_BOOLEANO, TokenCategory.PR_TIPO_CARACTERE,
				TokenCategory.PR_TIPO_INTEIRO, TokenCategory.PR_TIPO_REAL, TokenCategory.PR_TIPO_TEXTO);

	}

	public Moabson(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}

	private void updateToken() {
		this.currentToken = this.lexicalAnalyzer.nextToken();

		if (this.currentToken == null) {
			this.errorMsg("erro inesperado");
		}

		else {
			while (this.currentToken.getCategory() == null) {
				this.errorMsg("token não esperado");

				this.currentToken = this.lexicalAnalyzer.nextToken();
				if (this.currentToken == null) {
					this.errorMsg("erro inesperado");
					break;
				}
			}
		}

	}

	public boolean isSuccess() {
		return this.success;
	}

	public Boolean haveToken() {
		if (this.currentToken.getCategory() != TokenCategory.EOF) {
			return true;
		}

		return false;
	}

	private void errorMsg(String msg) {
		if (this.currentToken == null)
			System.out.println("erro inesperado");
		else
			System.out.println("token: " + this.currentToken.getLexicalValue() + "." + msg + ". linha "
					+ this.currentToken.getPosition().getLine() + ", coluna "
					+ this.currentToken.getPosition().getColumn() + ".");
		this.success = false;
	}
	
	public void Programa() {
		System.out.println("Programa = LOptDecl Inicio");
		
		LOptDecl();
		Inicio();
	}
	
	public void LOptDecl() {
		System.out.println("LOptDecl = LDeclGlob LFuncProc");
		
		LDeclGlob();
		LFuncProc();		
	}
	

	public void Escopo() {
		if (currentToken.getCategory() == TokenCategory.ABRE_CH) {
			System.out.println("Escopo = \"{\" LCmd \"}\"");
			updateToken();

			if (currentToken.getCategory() == TokenCategory.FECHA_CH) {
				updateToken();
				LCmd();
			} else {
				errorMsg("\"}\" esperado");
			}
		} else {
			errorMsg("\"{\" esperado");
		}

	}

	public void LCmd() {
		switch (currentToken.getCategory()) {
			case ID:
			case PR_CMD_DECL_VAR:
			case PR_CMD_DECL_CONST:
			case PR_CMD_SE:
			case PR_CMD_ENQUANTO:
			case PR_CMD_PARA:
				System.out.println("LCmd = Cmd LCmd");
				Cmd();
				LCmd();
				break;
				
			default:
				System.out.println("LCmd = epsilon");
				break;
		}
	}

	public void Cmd() {
		switch (currentToken.getCategory()) {
			case ID:
			case PR_CMD_DECL_VAR:
			case PR_CMD_DECL_CONST:
				CmdSemEscopo();
				
				if (currentToken.getCategory() == TokenCategory.SE_PONTOVIRGULA) {
					updateToken();
				} else {
					errorMsg("\";\" esperado");
				}
				
				break;
				
			case PR_CMD_SE:
			case PR_CMD_ENQUANTO:
			case PR_CMD_PARA:
				//updateToken();
				CmdComEscopo();
				break;

			default:
				errorMsg("token não esperado");
				break;
		}
	}

	public void CmdSemEscopo() {
		switch (currentToken.getCategory()) {
			case ID:
				updateToken();
				CmdSemEscopoR();
				break;
			
			case PR_CMD_DECL_VAR:
			case PR_CMD_DECL_CONST:
				Decl();
				break;

			default:
				errorMsg("token não esperado");
				break;
		}
	}
	
	public void CmdSemEscopoR() {
		switch (currentToken.getCategory()) {
			case ABRE_COL:
				System.out.println("CmdSemEscopoR = AcMatriz CmdSemEscopoR2");
				AcMatriz();
				CmdSemEscopoR2();
				break;
			
			case ABRE_CH:
				System.out.println("CmdSemEscopoR = ChFuncProcR");
				ChFuncProcR();
				break;
	
			default:
				errorMsg("token não esperado");
				break;
		}		
	}
	
	public void CmdSemEscopoR2() {
		switch (currentToken.getCategory()) {
			case OP_ATRIBUICAO:
				System.out.println("CmdSemEscopoR2 = AtribR");
				AtribR();
				break;
	
			default:
				System.out.println("CmdSemEscopoR2 = epsilon");
				break;
		}
	}


	public void CmdComEscopo() {
		switch (currentToken.getCategory()) {
			case PR_CMD_SE:
				System.out.println("CmdComEscopo = Se");
				Se();
				break;
				
			case PR_CMD_ENQUANTO:
				System.out.println("CmdComEscopo = Enquanto");
				Enquanto();
				break;
				
			case PR_CMD_PARA:
				System.out.println("CmdComEscopo = Para");
				Para();
				break;
	
			default:
				errorMsg("token não esperado");
				break;
		}
	}
	
	public void Se() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_SE) {
			System.out.println("Se = \"se\" TR6 Escopo Senao");
			updateToken();
			
			TR6();
			Escopo();
			Senao();
		}
	}
	
	public void Senao() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_SENAO) {
			System.out.println("Senao = \"senao\" Escopo");
			updateToken();
			
			Escopo();
		} else {
			System.out.println("Senao = epsilon");
		}
	}
	
	public void Enquanto() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_ENQUANTO) {
			System.out.println("Enquanto = \"enquanto\" \"(\" TR6 \")\" Escopo");
			
			if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
				updateToken();
				
				TR6();
				
				if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
					updateToken();
				
					Escopo();
				} else {
					errorMsg("\")\" esperado");
				}
			} else {
				errorMsg("\"(\" esperado");
			}
		}
	}
	
	public void Para() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_PARA) {
			System.out.println("Para = \"repita\" \"(\" ParaExpr \")\" \"ate\" \"(\" TR6 \")\" \"passo\" \"(\" ParaExpr \")\" Escopo");
			updateToken();
			
			if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
				updateToken();
				
				//ParaExpr();
				
				if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
					updateToken();
					
					if (currentToken.getCategory() == TokenCategory.PR_CMD_PARA_ATE) {
						updateToken();
						
						if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
							updateToken();
							
							TR6();
							
							if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
								updateToken();
								
								if (currentToken.getCategory() == TokenCategory.PR_CMD_PARA_PASSO) {
									updateToken();
									
									if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
										updateToken();
										
										// ParaExpr();
										
										if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
											updateToken();
											
											Escopo();
										} else {
											errorMsg("\")\" esperado");
										}
									} else {
										errorMsg("\"(\" esperado");
									}
								} else {
									errorMsg("\"passo\" esperado");
								}
							} else {
								errorMsg("\")\" esperado");
							}
						} else {
							errorMsg("\"(\" esperado");
						}
					} else {
						errorMsg("\"ate\" esperado");
					}
				} else {
					errorMsg("\")\" esperado");
				}
			} else {
				errorMsg("\"(\" esperado");
			}
		} else {
			errorMsg("\"para\" esperado");
		}
	}
	
	public void ParaExpr() {
		switch (currentToken.getCategory()) {
			case ID:
				System.out.println("ParaExpr = Atrib");
				Atrib();
				// ChVarConst() ?????????????????????
				break;
				
			case PR_CMD_DECL_VAR:
			case PR_CMD_DECL_CONST:
				System.out.println("ParaExpr = Decl");
				Decl();
				
			default:
				errorMsg("token não esperado");
				break;
		}
	
	}

	public void LDeclGlob() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_DECL_GLOBAL) {
			System.out.println("LDeclGlob = DeclGlob \";\" LDeclGlob");
			DeclGlob();
			
			if(currentToken.getCategory() == TokenCategory.SE_PONTOVIRGULA) {
				updateToken();
				
				LDeclGlob();
			} else {
				errorMsg("\";\" esperado");
			}			
		} else {
			System.out.println("LDeclGlob = epsilon");
		}
	}

	public void DeclGlob() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_DECL_GLOBAL) {
			System.out.println("DeclGlob = \"global\" Decl");

			updateToken();
			Decl();
		}
	}

	public void Decl() {
		System.out.println("Decl = ModDecl \"id\" \":\" DeclTipoAtrib");
		ModDecl();
		if (currentToken.getCategory() == TokenCategory.ID) {
			updateToken();
			if (currentToken.getCategory() == TokenCategory.SE_DOISPONTOS) {
				updateToken();
				DeclTipoAtrib();
			}
		} else {
			errorMsg("\"id\" inválido");
		}
	}

	public void ModDecl() {
		TokenCategory category = currentToken.getCategory();
		if (category == TokenCategory.PR_CMD_DECL_CONST) {
			System.out.println("ModDecl = \"const\"");
			updateToken();
		} else if (category == TokenCategory.PR_CMD_DECL_VAR) {
			System.out.println("ModDecl = \"var\"");			
			updateToken();
		} else {
			errorMsg("token não esperado");
		}
	}

	public void DeclTipoAtrib() {
		TokenCategory category = currentToken.getCategory();

		if (typesCategory.contains(category)) {
			System.out.println("DeclTipoAtrib = \"tipo\" DeclAtribTipo");
			updateToken();
	
			DeclAtribTipo();
		} else if (currentToken.getCategory() == TokenCategory.ABRE_CH) {
			System.out.println("DeclTipoAtrib = Matriz DeclAtribMatriz");
			
			Matriz();
			DeclAtribMatriz();
		} else {
			errorMsg("token não esperado");
		}

	}

	public void DeclAtribTipo() {
		if (currentToken.getCategory() == TokenCategory.OP_ATRIBUICAO) {
			System.out.println("DeclAtribTipo = \"opatrib\" VAtrib");
			updateToken();
			
			VAtrib();
		} else {
			System.out.println("DeclAtribTipo = epsilon");
		}
	}

	public void DeclAtribMatriz() {
		if (currentToken.getCategory() == TokenCategory.OP_ATRIBUICAO) {
			System.out.println("DeclAtribMatriz = \"opatrib\" \"{\" LArg \"}\"");
			updateToken();

			if (currentToken.getCategory() == TokenCategory.ABRE_CH) {
				updateToken();

				LArg();

				if (currentToken.getCategory() == TokenCategory.FECHA_CH) {
					updateToken();
				} else {
					errorMsg("\"}\" esperado");
				}
			} else {
				errorMsg("\"{\" esperado");			}
		} else {
			System.out.println("DeclAtribMatriz = epsilon");
		}
	}

	public void Matriz() {
		if (currentToken.getCategory() == TokenCategory.ABRE_COL) {
			System.out.println("Matriz = \"[\" \"tipo\" \";\" \"constInt\" \"]\"");
			updateToken();

			TokenCategory category = currentToken.getCategory();
			if (typesCategory.contains(category)) {
				updateToken();

				if (currentToken.getCategory() == TokenCategory.SE_PONTOVIRGULA) {
					updateToken();
					
					if (currentToken.getCategory() == TokenCategory.CONST_INT) {
						updateToken();
						
						if (currentToken.getCategory() == TokenCategory.FECHA_COL) {
							updateToken();
						} else {
							errorMsg("\"]\" esperado");
						}
					} else {
						errorMsg("\"constInt\" esperada");
					}
				} else {
					errorMsg("\";\" esperado");
				}
			} else {
				errorMsg("tipo inválido");
			}
		} else {
			errorMsg("\"[\" esperado");
		}
	}

	public void ChVarConst() {
		if (currentToken.getCategory() == TokenCategory.ID) {
			System.out.println("ChVarConst = \"id\" AcMatriz");
			updateToken();
			AcMatriz();
		}
	}

	public void AcMatriz() {
		if (currentToken.getCategory() == TokenCategory.ABRE_COL) {
            System.out.println("AcMatriz = \"[\"\"constInt\"\"]\"");
			updateToken();
			if (currentToken.getCategory() == TokenCategory.CONST_INT) {
				updateToken();
				if (currentToken.getCategory() == TokenCategory.FECHA_COL) {
					updateToken();
				} else {
					errorMsg("\"]\" esperado");
				}
			} else {
				errorMsg("\"constInt\" esperada");
			}
		} else {
			System.out.println("AcMatriz = epsilon");
		}
	}

	public void Retorno() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_RETORNE) {
            System.out.println("Retorno = \"retorne\"("+this.currentToken.getLexicalValue()+") VAtrib");
            updateToken();
			VAtrib();			
		} else {
			errorMsg("\"retorne\" esperado");
		}
	}

	public void Atrib() {
		if (currentToken.getCategory() == TokenCategory.ID) {
			System.out.println("Atrib = \"id\" AcMatriz AtribR");
			updateToken();
			
			AcMatriz();
			if (currentToken.getCategory() == TokenCategory.OP_ATRIBUICAO) {
				updateToken();
				
				AtribR();	
			} else {
				errorMsg("\"=\" esperado");
			}
		} else {
			errorMsg("\"id\" inválido");
		}
	}
	
	
	public void AtribR() {
		if (currentToken.getCategory() == TokenCategory.OP_ATRIBUICAO) {
			System.out.println("AtribR = \"opatrib\" VAtrib");
			updateToken();
			
			VAtrib();
		} else {
			errorMsg("\"=\" esperado");
		}
	}

	public void VAtrib() {
		System.out.println("VAtrib = Expr");
		Expr();
	}
	


	public void LParam() {
		if (currentToken.getCategory() == TokenCategory.ID) {
			System.out.println("LParam = Param LParamNr");
			
			Param();
			LParamNr();
		} else {
			System.out.println("LParamNr = epsilon");
		}
		
	}

	public void LParamNr() {
		if (currentToken.getCategory() == TokenCategory.SE_VIRGULA) {
			System.out.println("LParamNr = \",\" Param LParamNr");
			updateToken();

			Param();
			LParamNr();
		} else {
			System.out.println("LParamNr = epsilon");
		}
	}

	public void Param() {
		if (currentToken.getCategory() == TokenCategory.ID) {
			System.out.println("Param = \"id\" \":\" \"tipo\"");
			updateToken();
			if (currentToken.getCategory() == TokenCategory.SE_DOISPONTOS) {
				updateToken();
			
				TokenCategory category = currentToken.getCategory();

				if (typesCategory.contains(category)) {
					updateToken();
				} else {
					errorMsg("tipo inválido");
				}
				
			} else {
				errorMsg("\":\" esperado");
			}
		} else {
			errorMsg("\"id\" inválido");
		}
	}

	public void LFunc() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_FUNC) {
			System.out.println("LFunc = Func LFunc");
			Func();
			LFunc();
		} else {
			System.out.println("LFunc = epsilon");
		}
	}
	
	public void LFuncProc() {
		switch (currentToken.getCategory()) {
			case PR_CMD_FUNC:
				System.out.println("LFuncProc = LFunc LFuncProc");
				LFunc();
				LFuncProc();								
				break;
				
			case PR_CMD_PROC:
				LProc();
				LFuncProc();
				break;
	
			default:
				System.out.println("LFuncProc = epsilon");
				break;
		}

	}
	
	public void CorpoFunc() {
		System.out.println("CorpoFunc = LCmd Retorno");
		LCmd();
		Retorno();
	}

	public void Func() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_FUNC) {
			System.out.println("Func = \"funcao\" \"id\" \"(\" LParam \")\" \":\" \"tipo\" \"{\" CorpoFunc \";\" \"}\"");
			
			updateToken();
			if (currentToken.getCategory() == TokenCategory.ID) {
				updateToken();
				if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
					updateToken();
					LParam();

					if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
						updateToken();

						if (currentToken.getCategory() == TokenCategory.SE_DOISPONTOS) {
							updateToken();

							if (typesCategory.contains(currentToken.getCategory())) {
								updateToken();

								if (currentToken.getCategory() == TokenCategory.ABRE_CH) {
									updateToken();

									CorpoFunc();

									if (currentToken.getCategory() == TokenCategory.SE_PONTOVIRGULA) {
										updateToken();

										if (currentToken.getCategory() == TokenCategory.FECHA_CH) {
											updateToken();
										} else {
											errorMsg("\"}\" esperado");
										}
									} else {
										errorMsg("\";\" esperado");
									}
								} else {
									errorMsg("\"{\" esperado");
								}
							} else {
								errorMsg("tipo inválido");
							}
						} else {
							errorMsg("\";\" esperado");
						}
					} else {
						errorMsg("\")\" esperado");
					}
				} else {
					errorMsg("\"(\" esperado");
				}
			} else {
				errorMsg("\"id\" invalido");
			}
		}
	}

	public void LProc() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_PROC) {
			System.out.println("LProc = Proc LProc");
			Proc();
			LProc();
		} else {
			System.out.println("LProc = epsilon");
		}
	}

	public void Proc() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_PROC) {
			updateToken();
			if (currentToken.getCategory() == TokenCategory.ID) {
				updateToken();
				if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
					updateToken();
					LParam();

					if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
						updateToken();
						Escopo();
					} else {
						errorMsg("\")\" esperado");
					}
				} else {
					errorMsg("\"(\" inválido");
				}
			} else if (currentToken.getCategory() == TokenCategory.PR_INICIO) {
				//
			} else {
				errorMsg("\"id\" inválido");
			}
		}
	}

	public void ChFuncProcR() {
		if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
			System.out.println("ChFuncProcR = \"(\" LArg \")\"");
			updateToken();
			LArg();
			
			if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
				updateToken();
			} else {
				errorMsg("\")\" esperado");
			}
		} else {
			errorMsg("\"(\" esperado");
		}
	}

	public void Inicio() {
		if (currentToken.getCategory() == TokenCategory.PR_CMD_PROC) {
			System.out.println("Inicio = \"procedimento\" \"inicio\" \"(\" \")\" Escopo");
			updateToken();
			
			if (currentToken.getCategory() == TokenCategory.PR_INICIO) {
				updateToken(); 
	
				if (currentToken.getCategory() == TokenCategory.ABRE_PAR) {
					updateToken();
					
					if (currentToken.getCategory() == TokenCategory.FECHA_PAR) {
						updateToken();		
						
						Escopo();
					} else {
						errorMsg("\")\" esperado");
					}
				} else {
					errorMsg("\"(\" esperado");
				}
			} else {
				errorMsg("\"inicio\" esperado");
			}
		} else {
			errorMsg("\"procedimento\" esperado");
		}
	}

	public void LArg() {
		switch (currentToken.getCategory()) {
			case ABRE_PAR:
			case ID:
			case CONST_INT:
			case CONST_REAL:
			case CONST_CARACTERE:
			case CONST_TEXTO:
			case CONST_BOOL:
			case OP_ARIT_ADD:
			case OP_BOOL_NAO:
				System.out.println("LArg = VAtrib LArgNr");
				VAtrib();
				LArgNr();
				break;

			default:
				System.out.println("LArg = epsilon");
				break;
		}
	}

	public void LArgNr() {
		if (currentToken.getCategory() == TokenCategory.SE_VIRGULA) {
			System.out.println("LArgNr = \",\" VAtrib LArgNr");
			updateToken();
			
			VAtrib();
			LArgNr();
		} else {
			System.out.println("LArgNr = epsilon");
		}
	}

	public void Expr() {
		TokenCategory tkCateg = this.currentToken.getCategory();

		if (tkCateg == TokenCategory.ABRE_PAR) {
			System.out.println("Expr = TR");
			TR();
		}

		else if (tkCateg == TokenCategory.ID) {
			System.out.println("Expr = \"id\"(" + this.currentToken.getLexicalValue() + ") TR2 OPB");
			this.updateToken();
			TR2();
			OPB();
		}

		else if (tkCateg == TokenCategory.CONST_INT || tkCateg == TokenCategory.CONST_REAL
				|| tkCateg == TokenCategory.CONST_CARACTERE || tkCateg == TokenCategory.CONST_TEXTO) {
			System.out.println("Expr = NUMEROTEXTO OPB2");
			NUMEROTEXTO();
			OPB2();
		}

		else if (tkCateg == TokenCategory.CONST_BOOL) {
			System.out.println("Expr = \"constBool\"(" + this.currentToken.getLexicalValue() + ") OPB3");
			this.updateToken();
			OPB3();
		}

		else if (tkCateg == TokenCategory.OP_ARIT_ADD) {
			System.out.println("Expr = OPU");
			OPU();
		}

		else if (tkCateg == TokenCategory.OP_BOOL_NAO) {
			System.out.println("Expr = OPU2");
			OPU2();
		}

		else
			errorMsg("token não esperado");
	}

	private void TR() {
		// TODO Auto-generated method stub

	}

	private void OPU2() {
		// TODO Auto-generated method stub

	}

	private void OPU() {
		// TODO Auto-generated method stub

	}

	private void NUMEROTEXTO() {
		// TODO Auto-generated method stub

	}

	private void TR6() {
		// TODO Auto-generated method stub

	}

	private void TR11() {
		// TODO Auto-generated method stub

	}

	private void OPB3() {
		// TODO Auto-generated method stub

	}

	private void OPB2() {
		// TODO Auto-generated method stub

	}

	private void OPB() {
		// TODO Auto-generated method stub

	}

	private void TR2() {
		// TODO Auto-generated method stub

	}

	private void TR3() {
		// TODO Auto-generated method stub

	}

	public void run() {

		// if(this.updateToken()) {
		// System.out.println(this.currentToken.getLexicalValue());
		// }

		// TODO: remover após testes ~> arquivo testes.jcam
		updateToken();
		System.out.println(currentToken);
		LDeclGlob();
		
	}

}
