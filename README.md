# OnePercent  
Vinicius Bezerra - vbas
Matheus Casanova - mcnl

# Descrição do Projeto  

O OnePercent é um app mobile desenvolvido em ***Kotlin*** que indica rotas para fontes de energia mais próxima para recarregar seu dispositivo.  

O app está estruturado em 4 telas:
- Login
- Mapa
- Adicionar fonte de energia
- Scoreboard

## Método/Estruturas visto em aula

### Tela de login   

UI desenvolvida usando um componente RelativeLayout que estrutura:
- botão de log in (Google Sign In);
- botão de log out e;
- Imageview que mostra a logo OnePercent.

Nessa tela usou-se **Intents** para passar os dados de login da conta Google para a tela de Mapa. Também foi necessário manusear o Google Sign In na mesma *activity*, logo disparou-se a *activity* do login usando o método *startActivityForResult* e depois através do callback sobrecarregado *onActivityResult* consegue-se o resultado do Google Sign In, que também vem no Objeto de classe **Intent**.

[ IMAGEM TELA DE LOGIN]

### Tela de Mapa

Principal tela do aplicativo onde acontece tentou-se projetar maior parte da experiência do usuário. Após o login, está *activity* é disparada.

UI desenvolvida usando um componente *RelativeLayout* que comporta:
- *fragment* , que carrega o mapa;
- *FloatingActionButton* para adicionar fontes de energia e;
- *FloatingActionButton* para mostrar tela do Scoreboard.

[IMAGEM TELA DE MAPA]

Nesta tela acontece vários eventos. Há conectividade com o ***Firebase*** que é usado como "banco de dados" da aplicação. Nele persiste os dados dos usuários da aplicação e dos pontos de localização referente as fontes de energia.  

Logo, ao iniciar a *activity* é carregado o mapa e carregado  do *Firebase* os dados dos usuários e pontos das fontes de energia.

Plota-se no mapa os marcadores indicando as fontes de energia e, ao clicar em um marcador, uma rota é criada indicando um trajeto até a fonte de energia escolhida. Ao carregar o dispositivo, o marcador é removido do mapa para evitar desencontros para outros usuários.

Um dos *FloatingActionButton*  é usado para adicionar fontes de energia e é indicado com um "+". Este dispara um ***dialogView*** , que é "inflado" através do LayoutInflater e abre-se uma "janela" com um formulário sobre a fonte de energia. Com os dados da localização atual do usuário, adicionado do formulário, cria-se a fonte de energia no banco de dados para ser carregada no mapa e este usuário é recompensado com pontos no Scoreboard!

A UI do formulário é baseada em um *xml* e *LinearLayout* que comporta:
- Textfield para receber o lugar/localização da fonte;
- Textfield para a descrição da fonte e;
- Botão para enviar dados e cadastrar nova fonte de energia.

[TELA DO FORMULARIO]

Sobre o outro *FloatingActionButton* , que é usado para abrir o Scoreboard, tem um botão com "estrela" para indicar o Scoreboard. Este botão dispara a tela de Scoreboard, e na transição, passa os dados dos usuários, uma vez que nessa tela carregou-se os dados do banco de dados para a memória do aplicativo. Os dados são serializados em *Json* e enviados através da ***Intent*** para a tela de Scoreboard.

### Tela de Scoreboard

Esta tela é uma ***RecyclerView*** que mostra, em forma de lista, os usuários com seus scores.

A UI dessa tela é baseada em uma *ConstraintLayout* que comporta o component *RecyclerView*, e por sua vez, utiliza uma estrutura de lista personalizada que foi desenvolvida em um arquivo *xml* a parte que representa as linhas da tabela.

A UI da linha da tabela foi estruturada com:
- *ConstraintLayout* que comporta um:
	- *Cardview*, e que por sua vez comporta:
		- *RelativeLayout*, e este tem os campos:
			- *Imageview* que mostra a foto do usuário;
			- *Textfield* que mostra o nome do usuário e;
			- *Textfield* que mostra o score do usuário.

[IMAGEM TELA DE SCOREBOARD]

Além da *activity* é necessário desenvolver um ***Adapter*** e indicar a fonte de dados para o *RecyclerView*. Desenvolveu-se um *Adapter* que indica para o *RecyclerView* onde carregar cada informação e como renderizar a lista. 

A fonte de dados foi passada através da *Intent* da tela passada. Ela foi deserializada e então reconstruiu-se a lista de usuários para servir como fonte de dados para a *RecyclerView*. Junto com o ***LayoutManager*** e *Adapter*, a fonte de dados é renderizada corretamente na lista.

___  
