# Homefy

## Atores

- **Usuário**
  - Administrador da casa
  - Morador

---

## Requisitos Funcionais

### Autenticação e Conta

- [**RF01**]: Como usuário quero me autenticar no sistema utilizando e-mail e senha.
- [**RF02**]: Como usuário quero encerrar minha sessão atual.
- [**RF03**]: Como usuário quero me cadastrar no sistema utilizando:
  - E-mail
  - Senha
  - Nome
  - Nome de usuário
- [**RF04**]: Como usuário quero alterar meus dados cadastrais.
- [**RF05**]: Como usuário quero deletar minha conta do sistema.

---

### Casa

- [**RF06**]: Como usuário quero criar uma casa fornecendo nome e descrição.
- [**RF07**]: Como usuário quero entrar em uma casa utilizando um código de acesso.
- [**RF08**]: Como usuário quero sair de uma casa.
- [**RF09**]: Como administrador quero remover um morador da casa.
- [**RF10**]: Como administrador quero transferir a administração da casa para outro morador.
- [**RF11**]: Como administrador quero deletar a casa.
- [**RF12**]: Como administrador quero gerar e revogar o código de acesso da casa.

---

### Tarefas

- [**RF13**]: Como usuário quero criar uma tarefa doméstica fornecendo:
  - Título
  - Descrição
  - Responsável(is)
  - Frequência (única, diária, semanal, mensal)
  - Data/hora limite
- [**RF14**]: Como usuário quero visualizar todas as tarefas da casa.
- [**RF15**]: Como usuário quero visualizar apenas as tarefas atribuídas a mim.
- [**RF16**]: Como usuário quero marcar uma tarefa como concluída.
- [**RF17**]: Como usuário quero editar uma tarefa.
- [**RF18**]: Como usuário quero deletar uma tarefa.
- [**RF19**]: Como usuário quero visualizar o histórico de tarefas concluídas e por quem foram realizadas.

---

### Finanças — Contas

- [**RF20**]: Como usuário quero cadastrar uma conta a pagar fornecendo:
  - Título
  - Valor
  - Vencimento
  - Responsável pelo pagamento
  - Moradores que dividem o custo e a proporção de cada um
- [**RF21**]: Como usuário quero visualizar todas as contas da casa.
- [**RF22**]: Como usuário quero marcar uma conta como paga.
- [**RF23**]: Como usuário quero editar uma conta.
- [**RF24**]: Como usuário quero deletar uma conta.
- [**RF25**]: Como usuário quero visualizar o histórico de contas pagas.

---

### Finanças — Assinaturas

- [**RF26**]: Como usuário quero cadastrar uma assinatura fornecendo:
  - Nome do serviço
  - Valor total
  - Data de renovação
  - Moradores que utilizam e a proporção de cada um
- [**RF27**]: Como usuário quero visualizar todas as assinaturas da casa.
- [**RF28**]: Como usuário quero editar uma assinatura.
- [**RF29**]: Como usuário quero deletar uma assinatura.

---

### Finanças — Dashboard

- [**RF30**]: Como usuário quero visualizar um resumo financeiro mensal contendo:
  - Total de gastos da casa no mês
  - Quanto cada morador deve pagar
  - Quanto cada morador já pagou
- [**RF31**]: Como usuário quero visualizar o saldo entre moradores (quem deve para quem e quanto).

---

### Cozinha — Geladeira e Despensa

- [**RF32**]: Como usuário quero cadastrar um item na geladeira/despensa fornecendo:
  - Nome
  - Dono (morador ou compartilhado)
  - Quantidade
  - Data de validade (opcional)
- [**RF33**]: Como usuário quero visualizar todos os itens da geladeira/despensa organizados por dono.
- [**RF34**]: Como usuário quero editar um item da geladeira/despensa.
- [**RF35**]: Como usuário quero remover um item da geladeira/despensa.
- [**RF36**]: Como usuário quero registrar o consumo de um item alheio, gerando uma notificação ao dono.
- [**RF37**]: Como usuário quero ser notificado quando um item meu estiver próximo da data de validade.
- [**RF38**]: Como usuário quero definir uma quantidade mínima para um item compartilhado, sendo notificado quando ele estiver acabando.

---

### Cozinha — Lista de Feira

- [**RF39**]: Como usuário quero adicionar um item à lista de feira fornecendo:
  - Nome
  - Quantidade
  - Dono (meu ou compartilhado)
  - Responsável por comprar
  - Valor estimado (opcional)
- [**RF40**]: Como usuário quero visualizar a lista de feira da casa.
- [**RF41**]: Como usuário quero marcar um item da lista como comprado.
- [**RF42**]: Como usuário quero editar um item da lista de feira.
- [**RF43**]: Como usuário quero remover um item da lista de feira.
- [**RF44**]: Como usuário quero visualizar o total estimado da feira separado por morador (itens pessoais) e compartilhado.
- [**RF45**]: Como usuário quero mover itens comprados automaticamente para a geladeira/despensa ao concluir a feira.
- [**RF46**]: Como usuário quero visualizar o histórico de feiras anteriores.

---

### Itens e Espaços Pessoais

- [**RF47**]: Como usuário quero definir espaços da casa (ex: prateleira da geladeira, armário) e atribuí-los a um morador.
- [**RF48**]: Como usuário quero visualizar todos os espaços da casa e seus respectivos donos.
- [**RF49**]: Como administrador quero editar e remover espaços cadastrados.

---

### Notificações

- [**RF50**]: Como usuário quero receber notificações quando:
  - Uma conta está próxima do vencimento
  - Uma tarefa atribuída a mim está próxima do prazo
  - Um item meu na geladeira está próximo da validade
  - Um item compartilhado está abaixo da quantidade mínima
  - Alguém consumiu um item meu
  - Um novo item foi adicionado à lista de feira
  - Uma assinatura está próxima da renovação
  - Um novo morador entrou na casa