package analiseSintatica;

import java.util.ArrayList;
import analiseLexica.Token;
import analiseSemantica.Variavel;
import analiseSemantica.Funcao;

public class AnalisadorSintatico {

    private Token token; // Proximo token da lista
    private ArrayList<Token> tokens;    //lista com os tokens recebidos
    private ArrayList<String> erros;    //lista com os erros encontrados na an�lise.
    private ArrayList<String> errosSemanticos;    //lista com os erros encontrados na an�lise.

    private ArrayList<Funcao> funcoes = new ArrayList<>();

    private ArrayList<Variavel> variavel_global = new ArrayList<>();
    private ArrayList<Variavel> variavel_local = new ArrayList<>();
    private ArrayList<Variavel> variavel_local_erros = new ArrayList<>();
    private ArrayList<Variavel> parametrosCF = new ArrayList<>();
    private ArrayList<Variavel> constantes = new ArrayList<>();

    private int contTokens = 0;
    private String tip;
    private int aux = 0;
    private int atterror = 0;
    private int verific = 0;
    private int atribuicao = 0;
    private int tamanho = 0;
    private int tamanho_matriz = 0;
    private int verificaTamanho = 0;
    private String tipo_inco = null;
    private Funcao f = new Funcao();

    public void analise(ArrayList<Token> tokens) {

        this.tokens = tokens; //recebe os tokens vindos do lexico.
        token = proximo();  //recebe o primeiro token da lista
        erros = new ArrayList<>(); //cria a lista de erros
        errosSemanticos = new ArrayList<>();

        programa();

        if (!erros.isEmpty()) {
            System.out.println("Ocorreram erros na analise sintatica");
            System.out.println(erros); //imprime os erros na tela
        } else {
            System.out.println("Analise Sintatica feita com sucesso\n");
        }
        if (!errosSemanticos.isEmpty()) {
            System.out.println("Ocorreram erros na analise semantica");
            System.out.println(errosSemanticos); //imprime os erros na tela
        } else {
            System.out.println("Analise Semantica feita com sucesso\n");
        }

    }

    // Classe que inicializa o programa, verifica se exite a palavra program, se não existir nem executa o resto das verificacoes
    private void programa() {
        if (token.getLexema().equals("program")) {
            token = proximo();
            blocos();
        } else {
            erroSintatico("Esperava Program");
        }
    }

    // Classe responsavel por chamar os blocos permitidos fora da funcao
    private void blocos() {
        if (token.getLexema().equals("var")) {
            blocoVar();
        }
        if (token.getLexema().equals("const")) {
            blocoConst();
        }
        if (token.getLexema().equals("function")) {
            Funcao();
        } else {
            if (!token.getLexema().equals("EOF")) {
                erroSintatico("Esperava um: bloco de variavel|bloco de constates|bloco de funcao");
                token = proximo();
                if (!token.getLexema().equals("EOF")) {
                    blocos();
                }
            }
        }
    }

    private void erroSintatico(String erro) {
        if (!token.getLexema().equals("EOF")) {
            erros.add("Linha: " + (token.getLinha()) + " " + erro + "\n"); //gera o erro normalizado e adiciona na lista de erros.
        } else {
            erros.add(erro);
        }
    }

    private void erroSemantico(String erro) {
        if (!token.getLexema().equals("EOF")) {
            errosSemanticos.add("Linha: " + (token.getLinha()) + " " + erro + "\n"); //gera o erro normalizado e adiciona na lista de erros.
        } else {
            errosSemanticos.add(erro);
        }
    }

    private Token proximo() {
        if (contTokens < tokens.size()) { //verifica se ainda possuem tokens para a analise.
            return tokens.get(contTokens++);
        } else {
            return new Token(0, 0, "EOF", "EOF");  //cria um token de fim de arquivo.
        }
    }

    public ArrayList<String> getErros() {
        return this.erros;
    }

    public ArrayList<String> getErrosSemanticos() {
        return this.errosSemanticos;
    }

    //Bloco Variaveis globais
    private void blocoVar() {
        token = proximo();
        if (token.getLexema().equals("begin")) {
            token = proximo();
            corpoVar();
            // GlobaisAll();
        } else {
            erroSintatico("Bloco de variavel nao possui begin");
            if (!(token.getLexema().equals("const") || token.getLexema().equals("function") || token.getLexema().equals("end"))) {
                corpoVar();
            }

            if (token.getLexema().equals("end")) {
                token = proximo();
            }
        }
        if (token.getLexema().equals("const")) {
            blocoConst();
        }
        if (token.getLexema().equals("function")) {
            Funcao();
        }
    }

    private void corpoVar() {
        Variavel variavel = new Variavel();
        while (!(token.getLexema().equals("end") || token.getLexema().equals("const") || token.getLexema().equals("function"))) {
            if (tipo()) {
                tip = token.getLexema();
                token = proximo();
                declaracaoVariaveis();
                if (token.getLexema().equals("end") || token.getTipo().equals("Identificador") || token.getTipo().equals(",") || token.getTipo().equals(";")) {
                    break;
                }
            } else {
                erroSintatico("Falta palavra reservada: integer, cadeia, real, boolean, char");
                if (token.getTipo().equals("Identificador")) {
                    padraoVariavel();
                    if (!(pontoVirgula())) {
                        erroSintatico("Faltou ; "); //gera o erro se o tipo do token nao e o esperado
                        if (token.getLexema().equals("end")) {
                            return;
                        }
                        if (tipo()) {
                            token = proximo();
                            declaracaoVariaveis();
                        }
                    }
                } else if (token.getLexema().equals(",")) {
                    erroSintatico("Esperava um Id");
                    token = proximo();
                    padraoVariavel();
                } else {
                    token = proximo();
                    if (token.getTipo().equals("Identificador") || token.getLexema().equals("EOF") || token.getLexema().equals("const") || token.getLexema().equals("function")) {
                        if (token.getTipo().equals("Identificador")) {
                            erroSintatico("Esperava um tipo primitivo" + token.getLexema());
                            padraoVariavel();
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        if (token.getTipo().equals("Identificador")) {
            erroSintatico("Esperava um tipo primitivo");
            declaracaoVariaveis();

        } else if (!token.getLexema().equals("end")) {
            erroSintatico("Bloco de variaveis nao possui end");
        } else {
            token = proximo();
        }

    }   //id(complemento);

    private void declaracaoVariaveis() {
        padraoVariavel();
        // Caso ache um tipo apos um id e pq faltou ;   
        if (tipo()) {
            token = proximo();
            erroSintatico("Faltou ; ");
            declaracaoVariaveis();
        } else if (!(pontoVirgula())) {
            erroSintatico("Faltou ; "); //gera o erro se o tipo do token nao e o esperado
            if (token.getLexema().equals("end")) {
                return;
            }
            if (tipo()) {
                token = proximo();
                declaracaoVariaveis();
            } else if (token.getTipo().equals("Identificador")) {
                erroSintatico("Esperava um tipo primitivo aqui");
                padraoVariavel();
            } else {
                erroSintatico("Esperava tipo primitivo " + token.getLexema());
                token = proximo();
            }
        }

    }

    // GLOBAL id ou id,id ou id[1] ou id =1
    private void padraoVariavel() {
        Variavel variavel = new Variavel(), teste;
        if (token.getTipo().equals("Identificador")) {
            variavel.setTipo(tip);
            variavel.setNome(token.getLexema());
            tamanho = 0;
            token = proximo();
            atribuicao = 0;
            opcVariaveisGlo(variavel);
            variavel.setTamanho(tamanho);

            teste = variavelGlobalList(variavel.getNome());
            if (teste.getNome().equals("0")) {
                variavel_global.add(variavel);
            } else {
                erroSemantico("Variavel ja existe");
            }

        } else {
            if (token.getLexema().equals("=")) {
                token = proximo();
                igualdadeVarGlo(variavel);
            } else {
                if (!token.getLexema().equals(";")) {
                    erroSintatico("Lexema nao esperado: " + token.getLexema());
                    token = proximo();
                }
            }

        }
        if (token.getLexema().equals(",")) {
            token = proximo();
            padraoVariavel();
        } else if (token.getTipo().equals("Identificador")) {
            erroSintatico("Esperava ,");
            padraoVariavel();
        }
    }

    private void opcVariaveisGlo(Variavel variavel) {
        if (token.getLexema().equals("[")) {
            declaracaoMatriz(variavel);
        }
        if (token.getLexema().equals("=")) {
            token = proximo();
            igualdadeVarGlo(variavel);
        }
    }

    private void igualdadeVarGlo(Variavel variavel) {
        Variavel var = variavel;
        if (token.getLexema().equals("[")) {
            atribuicaoMatriz(var);
        } else if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true")) {

            if (token.getTipo().equals("char")) {
                if (!var.getTipo().equals("char")) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao de tipo incompativel");
                }
            } else if (token.getTipo().equals("string")) {
                if (!var.getTipo().equals("string")) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao de tipo incompativel");
                }
            } else if (token.getLexema().equals("true") || token.getLexema().equals("false")) {
                if (!var.getTipo().equals("boolean")) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao de tipo incompativel");
                }
            } else if (token.getTipo().equals("numero")) {
                if (token.getLexema().indexOf('.') != -1 && var.getTipo().equals("integer")) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao de tipo incompativel");
                } else if (!(var.getTipo().equals("integer") || var.getTipo().equals("real"))) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao de tipo incompativel");
                }
            }
            token = proximo();
        } else {
            erroSintatico("Atribuicao incorreta: " + token.getLexema());
            if (!(token.getLexema().equals(",") || token.getLexema().equals(";") || token.getLexema().equals("end"))) {
                token = proximo();
            }
        }

    }

    private void opcVariavel(Variavel local, String tipoVar) {

        if (token.getLexema().equals("[")) {
            declaracaoMatriz(local);
        }
        if (token.getLexema().equals("=")) {
            token = proximo();

            //  System.out.println("entrou aqui" + token.getLexema());
            igualdade(local, tipoVar);
        }

        Variavel var = variavelList(local.getNome());
        if (var.getNome().equals("0")) {
            local.setTipo(tipoVar);
            variavel_local.add(local);
            //ConstaAll();
        } else {
            erroSemantico("A variavel já existe no escopo local ");
        }

        if (token.getLexema().equals(",")) {
            virgulaL(local, tipoVar);
        } else if (token.getLexema().equals(";")) {

        }
    }

    private void igualdade(Variavel local, String tipoVar) {
        //verific = 0;
        tipo_inco = null;
        if (token.getLexema().equals("[")) {
            atribuicaoMatriz(local);
        } //Se for uma atribuicao normal);
        else if (token.getTipo().equals("Identificador") || token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true") || token.getLexema().equals("(")) {
            if (tipoVar.equals("string") && token.getTipo().equals("string")) {
                // verific = 0;
            } else if (tipoVar.equals("char") && token.getTipo().equals("char")) {
                // verific = 0;
            } else if (tipoVar.equals("boolean") && token.getLexema().equals("true") || token.getLexema().equals("false")) {
                // verific = 0;

            } else if (token.getTipo().equals("Identificador")) {
                if (!variavelList(token.getLexema()).getNome().equals("0")) {
                    Variavel v = variavelList(token.getLexema());
                    if (tipoVar.equals(v.getTipo())) {
                        //System.out.println("tipo ccerto");
                    } else if (v.getNome().equals("0")) {
                        //System.out.println("Variavel atribuida não existe");
                        erroSemantico("Variavel atribuida não existe");
                    } else {
                        //System.out.println("Atribuição com variavel de tipo incompativel");
                        erroSemantico("Atribuição com variavel de tipo incompativel");
                    }

                } else if (!constantList(token.getLexema()).equals("0")) {
                    Variavel v = constantList(token.getLexema());
                    //System.out.println(v.getTipo() + tipoVar + "tipo e tipo");
                    if (tipoVar.equals(v.getTipo())) {
                        // System.out.println("tipo ccerto");
                    } else if (v.getNome().equals("0")) {
                        erroSemantico("Variavel atribuida não existe");
                    } else {
                        erroSemantico("Atribuição com variavel de tipo incompativel");
                    }

                } else if (!variavelGlobalList(token.getLexema()).equals("0")) {
                    Variavel v = variavelGlobalList(token.getLexema());
                    if (tipoVar.equals(v.getTipo())) {
                        //System.out.println("tipo ccerto");
                    } else if (v.getNome().equals("0")) {
                        // System.out.println("Variavel atribuida não existe");
                        erroSemantico("Variavel atribuida não existe");
                    } else {
                        //System.out.println("Atribuição com Globvariavel de tipo incompativel");
                        erroSemantico("Atribuição com variavel de tipo incompativel");
                    }
                }

            } else if (!(token.getTipo().equals("numero") || token.getLexema().equals("("))) {

                //verific = 1;
            }
            if (token.getTipo().equals("numero") || token.getLexema().equals("(")) {
                if (tipoVar.equals("integer") && token.getTipo().equals("numero")) {
                    // verific = 0;
                } else {
                    erroSemantico("atribuição de tipo inteiro incompativel ");
                    //verific = 1;
                }
                tipo_inco = null;
                expLogica(0);
            } else {
                token = proximo();
                if (!(token.getLexema().equals(";") || token.getLexema().equals(","))) {
                    erroSintatico("Atribuicao incorreta: " + token.getLexema());
                    while (!(verifica() || token.getLexema().equals(";") || token.getLexema().equals("EOF"))) {
                        erroSintatico("Atribuicao incorreta: " + token.getLexema());
                    }
                }

            }

        } //Se for uma declaracao de variaveis locais pode receber um id e chamada de funcoes
        else {
            erroSintatico("atribuicao de tipo incompativel"); //gera o erro se o tipo do token nao e o esperado
        }

    }

    private boolean pontoVirgula() {
        if (token.getLexema().equals(";")) {
            token = proximo();
            return true;
        } else {
            return false;
        }
    }

    private void blocoConst() {
        token = proximo();
        if (token.getLexema().equals("begin")) {
            token = proximo();
            corpoConst();
        } else {
            erroSintatico("Bloco de constates nao possui begin");
            if (!(token.getLexema().equals("function") || token.getLexema().equals("end"))) {
                corpoConst();
            }

        }
        if (token.getLexema().equals("end")) {
            token = proximo();
        }
        if (token.getLexema().equals("function")) {
            Funcao();
        }
    }

    private void corpoConst() {
        while (!(token.getLexema().equals("end") || token.getLexema().equals("function") || token.getLexema().equals("EOF"))) {
            if (tipo()) {
                tip = token.getLexema();
                token = proximo();
                declaracao_const();
                if (token.getLexema().equals("end") || token.getTipo().equals("Identificador") || token.getTipo().equals(",") || token.getTipo().equals(";") || token.getLexema().equals("function")) {
                    if (!token.getLexema().equals("end")) {
                        erroSintatico("Esperava end");
                    }
                    break;
                } else {
                    token = proximo();
                }
            } else {
                erroSintatico("Esperava um tipo");
                if (token.getTipo().equals("Identificador")) {
                    declaracao_const();
                } else if (token.getLexema().equals(",")) {
                    erroSintatico("Esperava um id");
                    token = proximo();
                    declaracao_const();
                }
            }
            if (token.getLexema().equals("end")) {
                token = proximo();
                //ConstaAll();
            } else {
                erroSintatico("Bloco de constantes nao possui end");
            }
            if (token.getLexema().equals("function")) {
                Funcao();
            }
        }

    }

    private void declaracao_const() {
        padraoConst();
        if (token.getLexema().equals(";")) {
            token = proximo();
            if (tipo()) {
                tip = token.getLexema();
                token = proximo();
                declaracao_const();
            }
        } else {
            erroSintatico("Esperava ;");
            if (tipo()) {
                token = proximo();
                declaracao_const();
            }
        }

    }

    private void padraoConst() {
        Variavel constante = new Variavel();
        if (token.getTipo().equals("Identificador")) {
            constante.setTipo(tip);
            constante.setNome(token.getLexema());
            tamanho = 0;
            token = proximo();
            if (token.getLexema().equals("[")) {
                declaracaoMatriz(constante);
            }
            constante.setTamanho(tamanho);
            atribuicao = 0;
            if (token.getLexema().equals("=")) {
                token = proximo();
                if (token.getLexema().equals("[")) {
                    atribuicaoMatriz(constante);
                } else if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true")) {
                    if (token.getTipo().equals("char")) {
                        if (!constante.getTipo().equals("char")) {
                            erroSemantico("Atribuicao de tipo incompativel");
                            atribuicao = 1;
                        }
                    } else if (token.getTipo().equals("string")) {
                        if (!constante.getTipo().equals("string")) {
                            erroSemantico("Atribuicao de tipo incompativel");
                            atribuicao = 1;
                        }
                    } else if (token.getLexema().equals("true") || token.getLexema().equals("false")) {
                        if (!constante.getTipo().equals("boolean")) {
                            erroSemantico("Atribuicao de tipo incompativel");
                            atribuicao = 1;
                        }
                    } else if (token.getTipo().equals("numero")) {
                        if (token.getLexema().indexOf('.') != -1 && constante.getTipo().equals("integer")) {
                            atribuicao = 1;
                            erroSemantico("Atribuicao de tipo incompativel");
                        } else if (!(constante.getTipo().equals("integer") || constante.getTipo().equals("real"))) {
                            atribuicao = 1;
                            erroSemantico("Atribuicao de tipo incompativel");
                        }
                    }

                    token = proximo();
                } else {
                    erroSintatico("atribuicao de tipo incompativel"); //gera o erro se o tipo do token nao e o esperado
                    if (tipo()) {
                        tip = token.getLexema();
                        token = proximo();
                        declaracao_const();
                    } else if (!(token.getLexema().equals("end") || token.getLexema().equals(";"))) {
                        token = proximo();
                        if (token.getLexema().equals(",")) {
                            token = proximo();
                            padraoConst();
                        }
                    }
                }
                if (variavelGlobalList(constante.getNome()).getNome() == "0" && constantList(constante.getNome()).getNome() == "0") {
                    constantes.add(constante);
                } else {

                    erroSemantico("Variavel já existe no escopo global");
                }
                if (token.getLexema().equals(",")) {
                    token = proximo();
                    padraoConst();
                }
            } else {
                erroSintatico("Constantes precisam ser atribuidas"); //gera o erro se o tipo do token nao e o esperado
                if (token.getLexema().equals(",")) {
                    token = proximo();
                    padraoConst();
                }
            }

        } else {
            erroSintatico("Esperava Id");
            if (tipo()) {
                token = proximo();
                declaracao_const();
            }
            if (token.getLexema().equals("=")) {
                erroSintatico("Atribuicao incorreta");
                token = proximo();
                if (!(token.getLexema().equals(",") || token.getLexema().equals(";"))) {
                    token = proximo();
                    if (token.getLexema().equals(",")) {
                        token = proximo();
                        padraoConst();
                    }
                } else {
                    if (token.getLexema().equals(",")) {
                        token = proximo();
                        padraoConst();
                    }
                }
            } else {
                if (!token.getLexema().equals(";")) {
                    token = proximo();
                }
            }
        }

    }

    //Estrutura de uma funcao
    private void blocoFuncao() {
        Funcao funcao = new Funcao();

        token = proximo(); // pega o proximo que deveria ser um ID
        if (token.getTipo().equals("Identificador")) {
            funcao.setNome(token.getLexema());
            token = proximo();
            idFuncao(funcao);
        } else {
            erroSintatico("A funcao deve ser incializada com um nome");
            if (token.getLexema().equals("(")) {
                idFuncao(funcao);
            } else if (token.getLexema().equals(")")) {
                erroSintatico("Esperava (");
                parentesesF(funcao);
            } else if (token.getLexema().equals("integer") || token.getLexema().equals("real") || token.getLexema().equals("char") || token.getLexema().equals("string") || token.getLexema().equals("boolean")) {
                erroSintatico("Esperava (");
                parentesesF(funcao);
            } else if (token.getLexema().equals("begin")) {
                erroSintatico("Esperava (parametros)");
                fBegin();
            } else if (token.getLexema().equals(":")) {
                erroSintatico("Esperava (parametros)");
                temRetorno();
                if (token.getLexema().equals("begin")) {
                    fBegin();
                } else {
                    erroSintatico("Esperava begin");
                    while (!(token.getLexema().equals("end") || token.getLexema().equals("EOF") || token.getLexema().equals("function"))) {
                        erroSintatico("E necessario inicializar a funcao");
                        token = proximo();
                    }
                }
            } else if (token.getLexema().equals("end")) {
                erroSintatico("Esperava: (parametros)begin ");
                token = proximo();
            } else {
                if (!verifica()) {
                    //token = proximo();
                    if (!(token.getLexema().equals("EOF") || token.getLexema().equals("end"))) {
                        idFuncao(funcao);
                    }
                }
            }
            variavel_local.clear();
        }
    }

    private void idFuncao(Funcao funcao) {
        if (token.getLexema().equals("(")) {
            token = proximo();
            parentesesF(funcao);
        } else {
            erroSintatico("Faltou ( na funcao");
            if (token.getLexema().equals(")")) {
                parentesesF(funcao);
            } else if (token.getLexema().equals("integer") || token.getLexema().equals("real") || token.getLexema().equals("char") || token.getLexema().equals("string") || token.getLexema().equals("boolean")) {
                parentesesF(funcao);
            } else if (token.getLexema().equals("begin")) {
                erroSintatico("Esperava (parametros)");
                fBegin();
            } else if (token.getLexema().equals(":")) {
                erroSintatico("Esperava (parametros)");
                temRetorno();
                if (token.getLexema().equals("begin")) {
                    fBegin();
                } else {
                    erroSintatico("Esperava begin");
                    while (!(token.getLexema().equals("end") || token.getLexema().equals("EOF") || token.getLexema().equals("function"))) {
                        erroSintatico("E necessario inicializar a funcao");
                        token = proximo();
                    }
                }
            } else if (token.getLexema().equals("end")) {
                erroSintatico("Esperava: (parametros)begin ");
                token = proximo();
            } else {
                if (!verifica()) {
                    token = proximo();
                    if (!(token.getLexema().equals("EOF") || token.getLexema().equals("end"))) {
                        idFuncao(funcao);
                    }
                }
            }

        }
    }

    private void parentesesF(Funcao funcao) {
        ArrayList<Variavel> parametros = new ArrayList<>();
        if (!token.getLexema().equals(")")) {
            // obtem a lista de parametros da funcao
            parametros = parametroFuncao(parametros);
            funcao.setParametros(parametros);
        }
        if (token.getLexema().equals(")")) {
            token = proximo();
            if (token.getLexema().equals("integer") || token.getLexema().equals("real") || token.getLexema().equals("char") || token.getLexema().equals("string") || token.getLexema().equals("boolean")) {
                erroSintatico("Retorno incorreto, faltou :");
                token = proximo();
            }
        } else {
            erroSintatico("Faltou ) na funcao");
        }
        if (token.getLexema().equals(":")) {
            funcao.setTipoRetorno(temRetorno());
            //temRetorno();
        }
        if (!(token.getLexema().equals(":") || token.getLexema().equals("begin"))) {
            erroSintatico("Esperava begin ou retorno");
            token = proximo();
        }
        if (token.getLexema().equals("begin")) {
            // precisa verificar se a funcao existe 
            int a = existeFuncao(funcao);
            if(a == 1){
                erroSemantico("Funcao já existe");
            }else{
            funcoes.add(funcao);
            }
            f = funcao;
            fBegin();

        } else {
            erroSintatico("Faltou begin na funcao");
        }
    }

    private void fBegin() {

        token = proximo();
        bloco();
        while (!(token.getLexema().equals("EOF") || token.getLexema().equals("end") || token.getLexema().equals("function"))) {
            bloco();
        }
        if (token.getLexema().equals("function")) {
            erroSintatico("Funcoes nao podem ser declaradas dentro de funcoes");
            Funcao();
        }
        if (token.getLexema().equals("end")) {
            token = proximo();
        } else {
            erroSintatico("Faltou end na funcao");
        }
    }

    //Contem tudo que pode estar dentro de uma funcao
    private void bloco() {
        if (token.getLexema().equals("read")) {
            read();
        } else if (token.getLexema().equals("write")) {
            write();
        } else if (token.getTipo().equals("Identificador")) {
            opcId();
        } else if (token.getLexema().equals("if")) {
            ifElse();
        } else if (token.getLexema().equals("while")) {
            _while();
        } else if (tipo()) {
            String tipoVar = token.getLexema();
            token = proximo();
            nvariaveisL(tipoVar);
        } else if (token.getTipo().equals("numero")) {
            tipo_inco = null;
            exp2();
        } else if (token.getLexema().equals("true") || token.getLexema().equals("false")) {
            token = proximo();
            if (token.getLexema().equals(";")) {
                token = proximo();
            } else {
                erroSintatico("Esperava ;");
            }
        } else if (token.getLexema().equals("function")) {
            erroSintatico("Funcoes nao devem ser inicializadas dentro de funcoes");
            Funcao();
        } else if (token.getLexema().equals("EOF")) {

        } // Esta aceitando bloco vazio
        else if (token.getLexema().equals("end")) {

        } else {
            erroSintatico("Esperava componente do bloco, token: " + token.getLexema());
            token = proximo();
        }
        //variaveisAll();
    }

    private void exp2() {
        expLogica(0);
        if (!(pontoVirgula())) {
            erroSintatico("Faltou ; na atribuicao");
        }
    }

    private void nvariaveisL(String tipoVar) {
        variaveisLocais(tipoVar);
        if (token.getLexema().equals(";")) {
            token = proximo();
        } else {
            erroSintatico("Faltou ; ");
        }
    }

    private void variaveisLocais(String tipoVar) {
        if (token.getTipo().equals("Identificador")) {
            Variavel local = new Variavel();
            local.setNome(token.getLexema());
            local.setTipo(tipoVar);
            token = proximo();
            opcVariavel(local, tipoVar);

        } else {
            erroSintatico("Esperava Id");
        }

    }

    private void virgulaL(Variavel local, String tipoVar) {

        if (token.getLexema().equals(",")) {
            token = proximo();
            variaveisLocais(tipoVar);
        }
    }

    private void opcId() {
        String nome;
        int con = 0;
        if (token.getTipo().equals("Identificador")) {
            nome = token.getLexema();
            Variavel variavel = existeVar(nome);
            if (!constantList(nome).getNome().equals("0") && variavelList(nome).getNome().equals("0")) {
                con = 1;
            }
            this.atterror = 0;
            tipo_inco = null;
            expLogica(0);
            if (token.getLexema().equals("[")) {
                variavel = existeVar(nome);
                declaracaoMatriz(variavel);
                if (token.getLexema().equals("=")) {
                    if (con == 1) {
                        erroSemantico("Constantes nao podem ser atribuidas");
                    }
                    this.verific = 0;
                    variavel = existeVar(nome);
                    if (atterror != 0) {
                        erroSintatico("Atribuicao incorreta");
                    }
                    token = proximo();
                    if (atId(variavel)) {

                        if ((tipo_inco != null && variavel.getTipo() != null)) {
                            if (!variavel.getTipo().equals(tipo_inco)) {
                                erroSemantico("Atribuicao incorreta");
                            }
                        }
                    } else {
                        erroSintatico("Tipos incompativeis 1");
                    }
                    if (!(pontoVirgula())) {
                        erroSintatico("Faltou ; na atribuicao");
                    }
                }
            }
            if (token.getLexema().equals("(")) {
                System.out.println("Entra aqui ?");
                callFunction(0);
            } else if (token.getLexema().equals("->")) {
                if (!f.getNome().equals(nome)) {
                    erroSemantico("O nome do retorno deve ser igual ao da funcao");
                }
                token = proximo();
                retorno();
            } else if (token.getLexema().equals(";")) {
                token = proximo();
            } else if (token.getLexema().equals("=")) {
                if (con == 1) {
                    erroSemantico("Constantes nao podem ser atribuidas");
                }

                if (atterror != 0) {
                    erroSemantico("Atribuicao incorreta");
                }

                token = proximo();
                if (atId(variavel)) {

                    if ((tipo_inco != null && variavel.getTipo() != null)) {

                        if (!variavel.getTipo().equals(tipo_inco)) {
                            if (variavel.getTipo().equals("boolean") && (tipo_inco.equals("true") || tipo_inco.equals("false"))) {

                            } else if ((variavel.getTipo().equals("integer") || variavel.getTipo().equals("real")) && tipo_inco.equals("numero")) {

                            } else {
                                erroSemantico("Tipos incompativeis");

                            }

                        }
                    }
                } else {
                    erroSintatico("Atribuicao incorreta");
                }
                if (!(pontoVirgula())) {
                    erroSintatico("Faltou ; na atribuicao");
                }
            }
        } else {
            erroSintatico("Esperava um Id");
        }
    }

    // a=a[1]; n=1 n=1+1 n="char" 
    private boolean atId(Variavel variavel) {
        if (token.getLexema().equals("[")) {
            atribuicaoMatrizLocal();
            return true;
        }
        if (token.getTipo().equals("char") && variavel.getTipo().equals("char") || token.getTipo().equals("string") && variavel.getTipo().equals("string") || token.getLexema().equals("false") && variavel.getTipo().equals("boolean") || token.getLexema().equals("true") && variavel.getTipo().equals("boolean")) {

            if (token.getLexema().equals("true") || token.getLexema().equals("false")) {
                tipo_inco = token.getLexema();
            } else {
                tipo_inco = token.getTipo();
            }
            token = proximo();
            return true;
        } else if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getLexema().equals("false") || token.getLexema().equals("true")) {
            erroSemantico("Atribuicao imcompativel");
            token = proximo();
            return true;
        } else if (token.getLexema().equals("-") || token.getLexema().equals("!") || token.getTipo().equals("Identificador") || token.getTipo().equals("numero") || token.getLexema().equals("(")) {
            tipo_inco = null;

            expLogica(0);
            //System.out.println("Tipo da expressao: " + tipo_inco);
            return true;
        } else {
            return false;
        }
    }

    //Falta parenteses
    private void expID() {
        idLogic();
        if ((token.getLexema().equals("||") || token.getLexema().equals("&&")) && aux == 0) {
            token = proximo();
            this.verific++;
            if (!idLogic()) {
                erroSintatico("Esperava uma expressao relacional ");
            }
        }
    }

    private boolean idLogic() {
        if (exp()) {
            if (operadorRelacional()) {
                this.verific++;
                if (exp()) {
                    return true;
                } else {
                    erroSintatico("Esperava expressão");
                }
            }
            return false;
        }
        return false;
    }

    private void atribuicaoMatrizLocal() {
        token = proximo();
        if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true") || token.getTipo().equals("Identificador")) {
            if (token.getTipo().equals("numero") || token.getTipo().equals("Identificador")) {
                expLogica(0);
            } else {
                token = proximo();
            }

            if (token.getLexema().equals(",")) {
                atribuicaoMatrizLocal();
            } else if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true")) {
                erroSintatico("Esperava ,");
                token = proximo();
                atribuicaoMatrizLocal();
            } else {
                if (token.getLexema().equals("]")) {
                    token = proximo();
                } else {
                    while (!(token.getLexema().equals("]") || token.getLexema().equals("EOF") || token.getLexema().equals(",") || token.getLexema().equals(";") || token.getLexema().equals("end"))) {
                        erroSintatico("Esperava , ou ]");
                        token = proximo();
                    }
                    if (token.getLexema().equals(";")) {
                        erroSintatico("Esperava ]");
                    }
                    if (token.getLexema().equals("]")) {
                        token = proximo();
                    }
                }
            }
        } else {
            erroSintatico("atribuicao de matriz incorreta");
            if (token.getLexema().equals("]")) {
                token = proximo();
            } else {
                while (!(token.getLexema().equals("]") || token.getLexema().equals("EOF") || token.getLexema().equals(",") || token.getLexema().equals(";"))) {
                    erroSintatico("Esperava ]");
                    token = proximo();
                }
                if (token.getLexema().equals("]")) {
                    token = proximo();
                } else if (token.getLexema().equals(",")) {
                    atribuicaoMatrizLocal();
                }
            }
        }

    }

    private void estruturaCondicional() {
        if (token.getLexema().equals("begin")) {
            token = proximo();
            while (!(token.getLexema().equals("EOF") || token.getLexema().equals("end") || token.getLexema().equals("function"))) {
                bloco();
            }
            if (token.getLexema().equals("function")) {
                erroSintatico("Funcoes nao podem ser declaradas dentro de funcoes");
                Funcao();
            }
            if (token.getLexema().equals("end")) {
                token = proximo();
            } else {
                erroSintatico("Esperava end");
            }
        } else {
            erroSintatico("Esperava begin");
            if (verifica()) {
                while (!(token.getLexema().equals("EOF") || token.getLexema().equals("end") || token.getLexema().equals("function"))) {
                    bloco();
                }
                if (token.getLexema().equals("end")) {
                    token = proximo();
                } else {
                    erroSintatico("Esperava end");
                }
            }
        }
    }

    private void _while() {
        if (token.getLexema().equals("while")) {
            token = proximo();
            if (token.getLexema().equals("(")) {
                token = proximo();
                if (token.getLexema().equals(")")) {
                    erroSintatico("Esperava parametro");
                }
                parametrosIf();
                if (token.getLexema().equals(")")) {
                    token = proximo();
                    if (token.getLexema().equals("do")) {
                        token = proximo();
                        estruturaCondicional();
                    } else {
                        erroSintatico("Esperava do");
                        if (token.getLexema().equals("begin")) {
                            estruturaCondicional();
                        } else {
                            token = proximo();
                            estruturaCondicional();
                        }
                    }
                } else {
                    erroSintatico("Esperava )");
                    if (token.getLexema().equals("do")) {
                        token = proximo();
                        estruturaCondicional();
                    } else {
                        erroSintatico("Esperava do");
                        if (token.getLexema().equals("begin")) {
                            estruturaCondicional();
                        } else {
                            token = proximo();
                            estruturaCondicional();
                        }
                    }
                }
            } else {
                erroSintatico("Esperava (");
                if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                    parametrosIf();
                    if (token.getLexema().equals(")")) {
                        token = proximo();
                        if (token.getLexema().equals("do")) {
                            token = proximo();
                            estruturaCondicional();
                        } else {
                            erroSintatico("Esperava do");
                        }
                    } else {
                        erroSintatico("Esperava )");
                    }
                } else if (token.getLexema().equals(")")) {
                    erroSintatico("Esperava parametro");
                    fecP();
                }
            }
        }

    }

    private void fecP() {
        if (token.getLexema().equals(")")) {
            token = proximo();
            if (token.getLexema().equals("do")) {
                token = proximo();
                estruturaCondicional();
            } else {
                erroSintatico("Esperava do");
            }
        } else {
            erroSintatico("Esperava )");
        }
    }

    private void ifElse() {
        _If();
        _Else();

    }

    private void _If() {
        if (token.getLexema().equals("if")) {
            token = proximo();
            if (token.getLexema().equals("(")) {
                token = proximo();
                if (token.getLexema().equals(")")) {
                    erroSintatico("Esperava parametros if");
                }
                parametrosIf();
                fechaPar();
            } else {
                erroSintatico("Esperava (");
                if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                    parametrosIf();
                    fechaPar();
                } else if (token.getLexema().equals(")")) {
                    erroSintatico("Esperava parametros do if");
                    token = proximo();
                    if (token.getLexema().equals(";")) {
                        token = proximo();
                    } else {
                        erroSintatico("Esperava ;");
                    }
                }
            }
        }

    }

    private void fechaPar() {
        if (token.getLexema().equals(")")) {
            token = proximo();
            if (token.getLexema().equals("then")) {
                token = proximo();
                estruturaCondicional();
            } else {
                erroSintatico("Esperava then");
                if (token.getLexema().equals("begin")) {
                    estruturaCondicional();
                } else {
                    token = proximo();
                    estruturaCondicional();
                }

            }

        } else {
            erroSintatico("Esperava ) ");
            if (token.getLexema().equals("then")) {
                token = proximo();
                estruturaCondicional();
            } else {
                erroSintatico("Esperava then");
                estruturaCondicional();
            }
        }
    }

    private void _Else() {
        if (token.getLexema().equals("else")) {
            token = proximo();
            estruturaCondicional();
        }
    }

    //Opcoes de parametros if ou while
    private void parametrosIf() {
        tipo_inco = null;
        expLogica(1);
    }

    private ArrayList parametroFuncao(ArrayList<Variavel> parametros) {
        Variavel parametro = new Variavel();
        Variavel existeVar = new Variavel();
        if (tipoPrimitivo()) {
            parametro.setTipo(token.getLexema());
            token = proximo();
            if (token.getTipo().equals("Identificador")) {
                existeVar = variavelList(token.getLexema());
                parametro.setNome(token.getLexema());
                token = proximo();
                if (token.getLexema().equals("[")) {
                    tamanho = 0;
                    declaracaoMatriz(parametro);
                    parametro.setTamanho(tamanho);
                }
                if (existeVar.getNome().equals("0")) {
                    parametros.add(parametro);
                    variavel_local.add(parametro);

                } else {
                    erroSemantico("Variavel ja existe no escopo local");
                }
                if (token.getLexema().equals(",")) {
                    token = proximo();
                    parametroFuncao(parametros);
                } else if (!token.getLexema().equals(")")) {
                    parametroFuncao(parametros);
                } else {
                    return parametros;
                }
            } else {
                erroSintatico("Parametro de funcao incorreto");
            }
        } else {
            if (token.getTipo().equals("Identificador")) {
                token = proximo();
                if (token.getLexema().equals(",")) {
                    token = proximo();
                    parametroFuncao(parametros);
                }
            } else {
                erroSintatico("Parametro incorreto: " + token.getLexema());
                token = proximo();
                if (token.getTipo().equals("Identificador")) {
                    token = proximo();
                    if (token.getLexema().equals(",")) {
                        token = proximo();
                        parametroFuncao(parametros);
                    }
                }
                if (!(token.getLexema().equals(":") || token.getLexema().equals(")") || token.getLexema().equals("EOF") || token.getLexema().equals("function"))) {
                    parametroFuncao(parametros);
                }
                if (token.getLexema().equals("function")) {
                    Funcao();
                }
            }
        }
        return parametros;
    }

    private String temRetorno() {
        String tipoRetorno = "";
        token = proximo();
        if (tipoPrimitivo()) {
            tipoRetorno = token.getLexema();
            token = proximo();
            return tipoRetorno;
        } else {
            return tipoRetorno;
        }
    }

    private void retorno() {
        if (token.getLexema().equals("(")) {
            token = proximo();
            parametroRetorno();

            if (token.getLexema().equals(")")) {
                token = proximo();
            } else {
                erroSintatico("Esperava )");

            }

        } else {
            erroSintatico("Esperava (");
            if (token.getTipo().equals("numero") || token.getTipo().equals("Identificador") || token.getTipo().equals("string") || token.getTipo().equals("char") || token.getLexema().equals("true") || token.getLexema().equals("false")) {
                parametroRetorno();
                if (token.getLexema().equals(")")) {
                    token = proximo();
                } else {
                    erroSintatico("Esperava )");
                }
            } else if (token.getLexema().equals(")")) {
                token = proximo();
            }
        }
        if (!token.getLexema().equals(";")) {
            erroSintatico("Esperava ;");
            while (!(verifica() || token.getLexema().equals(";") || token.getLexema().equals(")") | token.getLexema().equals("EOF"))) {
                erroSintatico("Retorno incorreto");
            }
            if (token.getLexema().equals(")")) {
                token = proximo();
                if (!(token.getLexema().equals(";"))) {
                    erroSintatico("Esperava ;");
                } else {
                    token = proximo();
                }

            }
        } else {
            token = proximo();
        }

    }

    private void parametroRetorno() {

        if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero") || token.getTipo().equals("string") || token.getTipo().equals("char") || token.getLexema().equals("true") || token.getLexema().equals("false")) {
            if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                if (token.getTipo().equals("Identificador")) {
                    Variavel var = existeVar(token.getLexema());
                    if (var.getNome().equals("0")) {
                        erroSemantico("Variavel nao existe");
                    } else if (!var.getTipo().equals(f.getTipoRetorno())) {
                        erroSemantico("Retorno incorreto");
                    }

                }
                tipo_inco = null;
                exp();
            } else {
                if (!token.getTipo().equals(f.getTipoRetorno())) {
                    if (token.getLexema().equals("true") || token.getLexema().equals("false")) {
                        if (!f.getTipoRetorno().equals("boolean")) {
                            erroSemantico("Retorno incorreto");
                        }
                    } else {
                        erroSemantico("Retorno incorreto");
                    }
                }

                token = proximo();
            }
            if (token.getLexema().equals(",")) {
                erroSintatico("Retorno incorreto: " + token.getLexema());
                token = proximo();
                if (!(token.getLexema().equals(")") || token.getLexema().equals(";"))) {
                    erroSintatico("Retorno incorreto: " + token.getLexema());
                    token = proximo();
                }
            } else if (!(token.getLexema().equals(")") || token.getLexema().equals(";"))) {
                erroSintatico("Retorno incorreto: " + token.getLexema());
                token = proximo();
            }
        } else if (!token.getLexema().equals(")")) {
            erroSintatico("Esperava parametro do retorno ou )");
            verifica();
        }
    }

    private boolean verifica() {
        if (token.getLexema().equals("read") || token.getLexema().equals("write") || token.getLexema().equals("if") || token.getLexema().equals("while") || token.getTipo().equals("Identificador") || tipo()) {
            bloco();
            return true;
        } else {
            token = proximo();
            return false;
        }
    }

    private void Funcao() {
        variavel_local.clear();
        blocoFuncao();
        if (token.getLexema().equals("function")) {
            Funcao();
        }
    }

    private boolean tipo() {
        switch (token.getLexema()) {
            case "integer":
                //terminal("integer");
                return true;

            case "string":
                //terminal("string");
                return true;

            case "real":
                //terminal("real");
                return true;
            case "boolean":
                //terminal("boolean");
                return true;
            case "char":
                //terminal("char");
                return true;
            default:
                return false;
        }

    }

    public boolean tipoPrimitivo() {
        switch (token.getLexema()) {
            case "integer":
                return true;
            case "string":
                return true;
            case "real":
                return true;
            case "boolean":
                return true;
            case "char":
                return true;

            default:
                erroSintatico("falta palavra reservada: integer, cadeia, real, boolean, char");
                return false;
        }
    }

    private void declaracaoMatriz(Variavel variavel) {
        switch (token.getLexema()) {
            case "[":
                tamanho++;
                token = proximo();
                if (token.getTipo().equals("numero")) {
                    if (tamanho_matriz == 0) {
                        tamanho_matriz += Integer.parseInt(token.getLexema());
                    } else {
                        tamanho_matriz = Integer.parseInt(token.getLexema()) * tamanho_matriz;
                    }
                    token = proximo();
                } else if (!(token.getTipo().equals("numero"))) {
                    erroSintatico("declaracao de matriz incorreta, esperava numero");
                    if (token.getLexema().equals("[")) {
                        declaracaoMatriz(variavel);
                    } else if (token.getLexema().equals("]") || token.getLexema().equals(",") || token.getLexema().equals("=")) {

                    } else if (token.getLexema().equals("Identificador")) {
                        token = proximo();
                    } else {
                        while (!(token.getLexema().equals(";") || token.getLexema().equals("]") || token.getLexema().equals("EOF") || token.getLexema().equals("=") || token.getLexema().equals("end"))) {
                            erroSintatico("parametro matriz incorreto: " + token.getLexema());
                            token = proximo();
                        }
                    }
                }
                if (token.getLexema().equals("]")) {
                    token = proximo();
                } else {
                    erroSintatico("Esperava um ]");
                    if (token.getLexema().equals(",")) {
                        break;
                    } else if (token.getLexema().equals("=")) {
                        break;
                    } else if (token.getLexema().equals("[")) {
                        declaracaoMatriz(variavel);
                    } else {
                        while (!(token.getLexema().equals(";") || token.getLexema().equals("]") || token.getLexema().equals("EOF") || token.getLexema().equals("=") || token.getLexema().equals("end"))) {
                            erroSintatico("parametro matriz incorreto: " + token.getLexema());
                            token = proximo();
                        }
                        if (token.getLexema().equals("]")) {
                            token = proximo();
                        }
                    }
                }

                if (token.getLexema().equals("[")) {
                    declaracaoMatriz(variavel);
                }
                break;
            default:
                break;
        }
    }

    private void atribuicaoMatriz(Variavel var) {
        Variavel attmatriz = var;
        String tipo = var.getTipo();
        token = proximo();
        if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true")) {
            if (token.getTipo().equals("char")) {
                if (!attmatriz.getTipo().equals("char")) {
                    erroSemantico("Atribuicao em matriz de tipo incompativel");
                    atribuicao = 1;
                }
            } else if (token.getTipo().equals("string")) {
                if (!attmatriz.getTipo().equals("string")) {
                    erroSemantico("Atribuicao em matriz de tipo incompativel");
                    atribuicao = 1;
                }
            } else if (token.getTipo().equals("false") || token.getTipo().equals("true")) {
                if (!attmatriz.getTipo().equals("boolean")) {
                    erroSemantico("Atribuicao em matriz de tipo incompativel");
                    atribuicao = 1;
                }
            } else if (token.getTipo().equals("numero")) {
                if (token.getLexema().indexOf('.') != -1 && attmatriz.getTipo().equals("integer")) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao em matriz de tipo incompativel");
                } else if (!(attmatriz.getTipo().equals("integer") || attmatriz.getTipo().equals("real"))) {
                    atribuicao = 1;
                    erroSemantico("Atribuicao em matriz de tipo incompativel");
                }
            }
            verificaTamanho++;
            token = proximo();
            if (token.getLexema().equals(",")) {
                atribuicaoMatriz(attmatriz);
            } else if (token.getTipo().equals("char") || token.getTipo().equals("string") || token.getTipo().equals("numero") || token.getLexema().equals("false") || token.getLexema().equals("true")) {
                erroSintatico("Esperava ,");
                token = proximo();
                atribuicaoMatriz(attmatriz);
            } else {
                if (token.getLexema().equals("]")) {
                    if (verificaTamanho != tamanho_matriz) {
                        erroSemantico("Atribuição em matriz de tamanho incorreto");
                        verificaTamanho = 0;
                        tamanho_matriz = 0;

                    }
                    token = proximo();
                    verificaTamanho = 0;
                    tamanho_matriz = 0;
                } else {
                    while (!(token.getLexema().equals("]") || token.getLexema().equals("EOF") || token.getLexema().equals(",") || token.getLexema().equals(";") || token.getLexema().equals("end"))) {
                        erroSintatico("Esperava , ou ]");
                        token = proximo();
                    }
                    if (token.getLexema().equals(";")) {
                        erroSintatico("Esperava ]");
                    }
                    if (token.getLexema().equals("]")) {
                        token = proximo();
                    }
                }
            }
        } else {
            erroSintatico("atribuicao de matriz incorreta");
            if (token.getLexema().equals("]")) {
                token = proximo();
            } else {
                while (!(token.getLexema().equals("]") || token.getLexema().equals("EOF") || token.getLexema().equals(",") || token.getLexema().equals(";"))) {
                    erroSintatico("Esperava ]");
                    token = proximo();
                }
                if (token.getLexema().equals("]")) {
                    token = proximo();
                } else if (token.getLexema().equals(",")) {
                    atribuicaoMatriz(attmatriz);
                }
            }
        }

    }

    private void write() {
        token = proximo();
        if (token.getLexema().equals("(")) {
            token = proximo();
            if (token.getLexema().equals(")")) {
                erroSintatico("Esperava parametro write");
            }
            parentesesWrite();
            if (token.getLexema().equals(";")) {
                token = proximo();
            }
        } else {
            erroSintatico("Comando write mal formado, esperava (");
            if (token.getTipo().equals("string") || token.getTipo().equals("char") || token.getTipo().equals("numero") || token.getTipo().equals("Identificador")) {
                if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                    exp();
                } else {
                    token = proximo();
                }
                if (token.getLexema().equals("(")) {
                    erroSintatico("Esperava ( apos write");
                    token = proximo();
                    parentesesWrite();

                } else if (token.getLexema().equals(",")) {
                    token = proximo();
                    parentesesWrite();
                } else if (token.getLexema().equals(")")) {
                    token = proximo();
                    if (token.getLexema().equals(";")) {
                        token = proximo();
                    } else {
                        erroSintatico("Faltou ;");
                    }
                }

            } else if (token.getLexema().equals(",")) {
                erroSintatico("Esperava: string,char,numero,id ");
                parentesesWrite();
            } else if (token.getLexema().equals(")")) {
                erroSintatico("Esperava: string,char,numero,id ");
                token = proximo();
                if (token.getLexema().equals(";")) {
                    token = proximo();
                } else {
                    erroSintatico("Esperava ;");
                }
            } else if (token.getLexema().equals(";")) {
                token = proximo();
            }
        }

    }

    private void parentesesWrite() {
        parametroWrite();
        if (!(token.getLexema().equals(")") || token.getLexema().equals(";"))) {
            if (!token.getLexema().equals(")")) {
                // adicionar as outras opcoes aqui
                erroSintatico("Comando write mal formado esperava )");

            } else {
                erroSintatico("Comando write mal formado esperava ;");
                bloco();

            }
        } else if (token.getLexema().equals(";")) {
            erroSintatico("expressao write mal formada esperava )");
            token = proximo();
        } else if (token.getLexema().equals(")")) {
            token = proximo();
            if (!token.getLexema().equals(";")) {
                erroSintatico("expressao write mal formada esperava ;");

            } else {
                token = proximo();

            }

        }
    }

    private void parametroWrite() {
        if (token.getTipo().equals("string") || token.getTipo().equals("char") || token.getTipo().equals("numero") || token.getTipo().equals("Identificador")) {
            if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                exp();
            } else {
                token = proximo();
            }
            if (token.getLexema().equals(",")) {
                token = proximo();
                parametroWrite();
            } else if (token.getTipo().equals("string") || token.getTipo().equals("char") || token.getTipo().equals("numero") || token.getTipo().equals("Identificador")) {
                erroSintatico("Esperava , ou )");
                token = proximo();
            } else if (!token.getLexema().equals(")")) {
                erroSintatico("Esperava , ou ) ");
            }
        } else {
            if (!token.getLexema().equals(")")) {
                erroSintatico("Parametro write incorreto: " + token.getLexema());
            }

            if (token.getLexema().equals(",")) {
                token = proximo();
                parametroWrite();
            } else if (!(token.getLexema().equals(")") || token.getLexema().equals(";"))) {
                token = proximo();
                parametroWrite();
            }
        }
    }

    private void read() {
        token = proximo();
        if (token.getLexema().equals("(")) {
            token = proximo();
            if (token.getLexema().equals(")")) {
                erroSintatico("Esperava parametro read");
            }
            parametroRead();
            readParentesis();
        } else {
            erroSintatico("read nao possui (");
            if (token.getTipo().equals("Identificador")) {
                parametroRead();
                readParentesis();
            }
        }
    }

    private void readParentesis() {
        if (!(token.getLexema().equals(")") || token.getLexema().equals(";") || token.getLexema().equals("EOF"))) {
            if (!token.getLexema().equals(")")) {
                erroSintatico("expressao read mal formada esperava )");
                if (token.getLexema().equals(";")) {
                    token = proximo();
                } else {
                    erroSintatico("Esperava ;");
                }
            }
        } else if (token.getLexema().equals(")")) {
            token = proximo();

            if (token.getLexema().equals("end")) {
                erroSintatico("Esperava ;");
            } else if (!token.getLexema().equals(";")) {
                erroSintatico("Esperava ;");
                while (!(token.getLexema().equals(";") || verifica() || token.getLexema().equals("end"))) {
                    erroSintatico("expressao read mal formada esperava ;");
                }
                if (token.getLexema().equals(";")) {
                    token = proximo();
                }
                // bloco();
            } else if (token.getLexema().equals(";")) {
                token = proximo();
            } else {
                token = proximo();
            }
        } else if (token.getLexema().equals(";")) {
            token = proximo();
        }

    }

    private void parametroRead() {
        Variavel variavel = new Variavel();

        if (token.getTipo().equals("Identificador")) {
            String nome = token.getLexema();
            token = proximo();
            variavel = existeVar(nome);
            if (variavel.getNome().equals("0")) {
                erroSemantico("Variavel nao declarada");
            }
            tamanho = 0;
            // procurar essa variavel na lista
            if (token.getLexema().equals("[")) {
                declaracaoMatriz(variavel);
                if (tamanho != variavel.getTamanho()) {
                    erroSemantico("Matriz nao existe");
                }
            }
            if (token.getLexema().equals(",")) {
                token = proximo();
                parametroRead();
            }
            if (token.getTipo().equals("Identificador")) {
                erroSintatico("Esperava ,");
                parametroRead();
            } else if (!(token.getLexema().equals(")") || token.getLexema().equals(";"))) {
                erroSintatico("Esperava Id ou ) ");
                token = proximo();
            } else if (token.getLexema().equals(";")) {
                erroSintatico("Esperava )");
            }
        } else {
            while (!(token.getLexema().equals(",") || token.getLexema().equals(")") || token.getLexema().equals(";") || token.getTipo().equals("Identificador") || token.getLexema().equals("EOF"))) {
                erroSintatico("Parametro read incorreto: " + token.getLexema() + " falta Id ");
                token = proximo();
            }
            if (token.getTipo().equals("Identificador")) {
                erroSintatico("Esperava ,");
                parametroRead();
            }
            if (token.getLexema().equals(",")) {
                erroSintatico("Parametro read incorreto: " + token.getLexema() + " falta Id ");
                token = proximo();
                parametroRead();
            }
        }
    }

    private void parameterCallFunction() {
        Variavel var = new Variavel();
        if (token.getTipo().equals("string") || token.getTipo().equals("char") || token.getLexema().equals("true") || token.getLexema().equals("false") || token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
            if(token.getLexema().equals("true")||(token.getLexema().equals("false"))){
                var.setTipo("boolean");
            }else if(token.getTipo().equals("char")||token.getTipo().equals("string")){
                var.setTipo(token.getTipo());
            }
           
            if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                tipo_inco = null;
                exp();
                var.setTipo(tipo_inco);
            } else {
                token = proximo();
            }
            parametrosCF.add(var);
            if (token.getLexema().equals(",")) {
                token = proximo();
                parameterCallFunction();
            }
        } else {
            erroSintatico("Parametro invalido :" + token.getLexema());
            if (!verifica()) {
                if (token.getLexema().equals(",")) {
                    token = proximo();
                    parameterCallFunction();
                }
            }
        }
    }

    private void callFunction(int opc1) {
        int opc = opc1;
        if (token.getLexema().equals("(")) {
            token = proximo();
            if (!token.getLexema().equals(")")) {
                parametrosCF.clear();
                parameterCallFunction();
            }
        }
        if (token.getLexema().equals(")")) {
            token = proximo();
            if (!token.getLexema().equals(";") && opc == 0) {
                erroSintatico("falta ;");
            } else {
                if (opc == 0) {
                    token = proximo();
                }
            }

        } else {
            erroSintatico("falta )");
            //colocar if verificando se o atual nao e alguma das outras opcoes
            token = proximo();
        }
    }

    //Expressoes Aritmeticas
    private boolean exp() {
        if (exp_A1()) {
            expAux1();
            return true;
        } else {
            return false;
        }

    }

    private boolean exp_A1() {
        if (valorNumerico()) {
            exp_Aux4();
            return true;
        } else {
            return false;
        }
    }

    private boolean expAux1() {
        if (exp_SomSub()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean exp_SomSub() {
        if (fator()) {
            if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero") || token.getLexema().equals("(")) {
                exp();
            } else {
                erroSintatico("Esperava um Id ou numero");
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean fator() {
        String ch = token.getLexema();
        char n = ch.charAt(0);
        if (n == '-' && token.getTipo().equals("numero")) {
            return true;
        } else if (token.getLexema().equals("+") || token.getLexema().equals("-")) {
            this.atterror++;
            token = proximo();

            return true;
        } else {
            return false;
        }
    }

    private boolean exp_Aux4() {
        if (exp_MultDiv()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean exp_MultDiv() {
        if (termo()) {
            token = proximo();
            if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero") || token.getLexema().equals("(")) {
                exp();
            } else {
                erroSintatico("Esperava um numero ou Id");
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean termo() {
        if (token.getLexema().equals("*") || token.getLexema().equals("/") || token.getLexema().equals("%")) {
            this.atterror++;
            this.verific++;
            return true;
        }
        return false;
    }

    private boolean valorNumerico() {
        if (token.getLexema().equals("(")) {
            token = proximo();
            exp();
            if (!token.getLexema().equals(")")) {
                erroSintatico("Faltou )");
                return true;
            } else {
                token = proximo();
                return true;
            }
        } else if (idAritmetico()) {
            return true;
        } else if (token.getTipo().equals("Identificador")) {
            verifId(token.getLexema());
            return true;
        } else if (token.getTipo().equals("numero")) {
            tipo_inco = token.getTipo();
            token = proximo();
            return true;
        } else {
            return false;
        }
    }

    private void verifId(String lexema) {
        //System.out.println(" Lexema recebido na expressao " + lexema);
        String nome = lexema;
        Variavel variavel = existeVar(lexema);
        Funcao fn = funcaotList(lexema);
        //System.out.println("Pegou var " + variavel.getNome() + " tipo da variavel " + variavel.getTipo());
        if (variavel.getNome().equals("0") && fn.getNome().equals("0")) {
            erroSemantico("Id ou função não existe");
        }
        if (tipo_inco == null) {
            tipo_inco = variavel.getTipo();
        } else if (tipo_inco != null && tipo_inco != "erro") {
            if (!tipo_inco.equals(variavel.getTipo()) && !variavel.getNome().equals("0")) {
                if (tipo_inco.equals("numero") && (variavel.getTipo().equals("inteiro") || variavel.getTipo().equals("real"))) {

                } else {
                    erroSemantico("expressão entre tipos incompativeis");
                    tipo_inco = "erro";
                }

            }

        }
        token = proximo();
        if (!token.getLexema().equals("->")) {

        }
        if (token.getLexema().equals("(")) {
            int teste;
            callFunction(1);
            teste = existeFuncao2(nome, parametrosCF);
            if(teste == 0){
                erroSemantico("Assinatura de funcao incorreta");
            }
        } else if (token.getLexema().equals("[")) {
            declaracaoMatriz(variavel);
        } else {

        }
    }

    private boolean idAritmetico() {
        if (token.getLexema().equals("-")) {
            token = proximo();
            if (token.getTipo().equals("Identificador") || token.getTipo().equals("numero")) {
                //verificar se o id existe

                token = proximo();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //Expressoes Relacionais
    public boolean operadorRelacional() {
        if (token.getLexema().equals("!=")) {
            token = proximo();
            return true;
        } else if (token.getLexema().equals(">")) {
            token = proximo();
            return true;
        } else if (token.getLexema().equals("<")) {
            token = proximo();
            return true;
        } else if (token.getLexema().equals(">=")) {
            token = proximo();
            return true;
        } else if (token.getLexema().equals("<=")) {
            token = proximo();
            return true;
        } else {
            return false;
        }
    }

    private boolean relacionan(int opc) {
        if (exp()) {
            if (token.getLexema().equals(")") && aux != 0) {
                token = proximo();
                this.aux--;
            }
            if (token.getLexema().equals("*") || token.getLexema().equals("/") || token.getLexema().equals("%") || token.getLexema().equals("-") || token.getLexema().equals("+")) {
                token = proximo();
                this.atterror++;
                this.verific++;
                exp();
            }
            //
            if (operadorRelacional()) {
                this.atterror++;
                this.verific++;
                if (exp()) {
                    //tipo_inco="boolean";
                    return true;
                } else {
                    erroSintatico("Esperava uma expressao");
                    return false;
                }
            } else if (token.getLexema().equals("==")) {
                token = proximo();
                if (token.getLexema().equals("true")) {
                    token = proximo();
                    tipo_inco="boolean";
                    return true;
                } else if (token.getLexema().equals("false")) {
                    token = proximo();
                    tipo_inco="boolean";
                    return true;
                } else if (exp()) {
                    tipo_inco="boolean";
                    return true;
                } else {
                    erroSintatico("Esperava expressao");
                    return true;
                }
            } else {
                if (opc == 1) {
                    erroSintatico("Esperava um operador relacional");
                    if (token.getLexema().equals("||") || token.getLexema().equals("&&")) {
                        token = proximo();
                        verific++;
                        auxExpLogica(1);
                        return false;
                    } else if (opc == 0) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private boolean expRelacional(int opc) {
        if (token.getLexema().equals("(")) {
            token = proximo();
            this.aux++;
            relacionan(opc);
            if (token.getLexema().equals(")")) {
                token = proximo();
                this.aux--;
                return true;
            } else {
                return true;
            }
        } else if (relacionan(opc)) {
            return true;
        } else {
            return false;
        }
    }

    //Expressoes logicas
    public boolean operadorLogico() {
        if (token.getLexema().equals("&&")) {
            token = proximo();
            this.atterror++;
            return true;
        } else if (token.getLexema().equals("||")) {
            token = proximo();
            this.verific++;
            this.atterror++;
            return true;
        } else {
            return false;
        }
    }

    private boolean auxExpLogica(int opc) {
        if (expRelacional(opc)) {
            if (token.getLexema().equals(")") && aux != 0) {
                token = proximo();
                this.aux--;
            }
            if (token.getLexema().equals("&&") || token.getLexema().equals("||")) {
                this.atterror++;
                this.verific++;
                token = proximo();
                if (expRelacional(1)) {
                    auxLog();
                    return true;
                } else {
                    erroSintatico("Esperava uma expressao relacional");
                    return false;
                }
            } else {
                return true; // sera so uma relacional
            }
        } else {
            if (opc == 1) {
                erroSintatico("Esperava expressao relacional");

            }
            return false;
        }

    }

    private boolean expLogica(int opc) {

        if (token.getLexema().equals("!")) {
            token = proximo();
            negx(opc);
            auxLog();

            validPare();
            return true;
        } else if (token.getLexema().equals("(")) {
            token = proximo();
            this.aux++;
            auxExpLogica(opc);
            if (token.getLexema().equals(")") && aux != 0) {
                this.aux--;
                token = proximo();
                validPare();
                return true;
            } else if (token.getLexema().equals(")") && aux == 0) {
                validPare();
                return true;
            } else {
                validPare();
                return true;
            }
        } else if (expRelacional(opc)) {
            if (token.getLexema().equals("&&") || token.getLexema().equals("||")) {
                token = proximo();
                this.verific++;
                if (expRelacional(1)) {
                    auxLog();
                    return true;
                } else {
                    erroSintatico("Esperava uma expressao relacional");
                    return false;
                }
            } else {
                return true; // sera so uma relacional
            }

        } else {
            return false;
        }

    }

    private void validPare() {
        if (aux > 0) {
            erroSintatico("Esperava )");
        } else if (aux < 0) {
            erroSintatico("Esperava (");
        } else {
        }
    }

    private void auxLog() {
        if (token.getLexema().equals("&&") || token.getLexema().equals("||")) {
            token = proximo();
            this.verific++;
            if (expLogica(1)) {

            } else {
                erroSintatico("Esperava uma expressao");
            }
        }
    }

    private void negx(int opc) {
        if (token.getLexema().equals("(")) {
            //this.aux++;
            token = proximo();
            expRelacional(opc);
            auxLog();
            if (token.getLexema().equals(")")) {
                //  this.aux--;
                token = proximo();
            } else {
                erroSintatico("Esperava )");
            }
        }
    }

    public void GlobaisAll() {
        System.out.println("Globais");
        for (int i = 0; i < variavel_global.size(); i++) //cars name of arraylist
        {

            System.out.println(variavel_global.get(i).getNome() + "   " + variavel_global.get(i).getTipo());

        }
        System.out.println("END LIST");
    }

    public void ConstaAll() {
        System.out.println("Constantes");
        for (int i = 0; i < constantes.size(); i++) //cars name of arraylist
        {

            System.out.println(constantes.get(i).getNome() + "   " + constantes.get(i).getTipo());

        }
        System.out.println("END LIST");
    }

    public void variaveisAll() {
        System.out.println("LISTAA");
        for (int i = 0; i < variavel_local.size(); i++) //cars name of arraylist
        {

            System.out.println(variavel_local.get(i).getNome() + "   " + variavel_local.get(i).getTipo());

        }
        System.out.println("END LIST");
    }

    public boolean variaveisContains(Variavel local) {

        for (int i = 0; i < variavel_local.size(); i++) //cars name of arraylist
        {

            if (variavel_local.get(i).getNome().equals(local.getNome())) {
                return true;
            }

        }
        return false;

    }

    public Variavel variavelList(String nome) {
        Variavel variavel = new Variavel();
        variavel.setNome("0");
        for (int i = 0; i < variavel_local.size(); i++) //cars name of arraylist
        {

            if (variavel_local.get(i).getNome().equals(nome)) {
                variavel = variavel_local.get(i);
                return variavel;
            }
        }
        return variavel;

    }

    public Variavel variavelGlobalList(String nome) {
        Variavel variavel = new Variavel();
        variavel.setNome("0");
        for (int i = 0; i < variavel_global.size(); i++) //cars name of arraylist
        {

            if (variavel_global.get(i).getNome().equals(nome)) {
                variavel = variavel_global.get(i);
                return variavel;
            }
        }
        return variavel;

    }

    public Variavel constantList(String nome) {
        Variavel constante = new Variavel();
        constante.setNome("0");
        for (int i = 0; i < constantes.size(); i++) //cars name of arraylist
        {

            if (constantes.get(i).getNome().equals(nome)) {
                constante = constantes.get(i);
                return constante;
            }
        }
        return constante;

    }

    public int existeFuncao2(String lexema, ArrayList<Variavel> param){
        int assinatura=0;
        int tamanho;
        ArrayList<Variavel> parametrosChamada = new ArrayList();
        ArrayList<Variavel> parametrosF = new ArrayList();
        for(int i = 0; i < funcoes.size(); i++){
              if (funcoes.get(i).getNome().equals(lexema)) {
                    parametrosF = funcoes.get(i).getParametros();
                    parametrosChamada = param;
                    if (parametrosF.size() == parametrosChamada.size()) {
                        tamanho = param.size();
                        for (int j = 0; j < param.size(); j++) {    
                            if (parametrosF.get(i).getTipo().equals(parametrosChamada.get(i).getTipo())) {
                                tamanho--;
                            }else if(parametrosF.get(i).getTipo().equals("integer")||parametrosF.get(i).getTipo().equals("real")){
                                if(parametrosChamada.get(i).getTipo().equals("numero")){
                                    tamanho --;
                                }
                            }
                        if(tamanho == 0){
                            assinatura = 1;
                        }
                    }
                }
                //funcao = funcoes.get(i);
                //return existe;
            }   
        }
        return assinatura;
    }
    public int existeFuncao(Funcao n) {
        Funcao funcao = n;
        int existe = 0;
        int parametrosn;
        ArrayList<Variavel> param = new ArrayList(), param2 = new ArrayList();
        for (int i = 0; i < funcoes.size(); i++) {
            if (funcoes.get(i).getNome().equals(funcao.getNome())) {
                if (funcoes.get(i).getTipoRetorno().equals(funcao.getTipoRetorno())) {
                    param = funcoes.get(i).getParametros();
                    param2 = funcao.getParametros();
                    if (param.size() == param2.size()) {
                        parametrosn = param.size();
                        for (int j = 0; j < param.size(); j++) {    
                            if (param.get(i).getTipo().equals(param2.get(i).getTipo())) {
                                parametrosn--;
                            }
                        }
                        if(parametrosn == 0){
                            existe = 1;
                        }
                    }
                }
                //funcao = funcoes.get(i);
                //return existe;
            }
        }
        return existe;

    }

    public Funcao funcaotList(String nome) {
        Funcao funcao = new Funcao();
        funcao.setNome("0");
        for (int i = 0; i < funcoes.size(); i++) //cars name of arraylist
        {
            if (funcoes.get(i).getNome().equals(nome)) {
                funcao = funcoes.get(i);
                return funcao;
            }
        }
        return funcao;

    }
// Primeiro procura na lista de locais

    public Variavel existeVar(String lexema) {
        Variavel constante, variavel, variavelGlobal, erro;
        constante = constantList(lexema);
        variavel = variavelList(lexema);
        variavelGlobal = variavelGlobalList(lexema);
        if (!variavel.getNome().equals("0")) {
            return variavel;
        } else if (!variavelGlobal.getNome().equals("0")) {
            return variavelGlobal;
        } else if (!constante.getNome().equals("0")) {
            return constante;
        } else {
            erro = new Variavel();
            erro.setNome("0");
            return erro;
        }
    }

}
