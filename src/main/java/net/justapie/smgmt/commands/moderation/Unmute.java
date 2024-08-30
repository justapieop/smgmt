package net.justapie.smgmt.commands.moderation;

import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.justapie.smgmt.Constants;
import net.justapie.smgmt.commands.VCommand;
import net.justapie.smgmt.database.MongoUtils;
import net.justapie.smgmt.database.models.Record;
import net.justapie.smgmt.enums.RecordType;
import net.justapie.smgmt.utils.config.Config;
import net.justapie.smgmt.utils.config.ConfigFormatter;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Unmute extends VCommand {
  public Unmute() {
    super("unmute");
  }

  @Override
  public BrigadierCommand makeBrigadierCommand(ProxyServer proxy) {
    return new BrigadierCommand(
      BrigadierCommand.literalArgumentBuilder(this.name)
        .requires(src -> src.hasPermission("smgmt.moderation.unmute"))
        .then(
          Constants.getPlayerArg(proxy)
            .executes(
              ctx -> {
                String username = ctx.getArgument("player", String.class);

                List<Record> records = MongoUtils.getRecords(username, RecordType.MUTE);

                if (records.isEmpty()) {
                  ctx.getSource().sendPlainMessage(
                    new ConfigFormatter(
                      Config.getMessageNode().node("noRecord").getString()
                    )
                      .putKV("player", username)
                      .build()
                  );
                  return Command.SINGLE_SUCCESS;
                }

                Record latestRecord = records.getFirst();

                if (!Objects.isNull(latestRecord.getExpiredOn()) || (!latestRecord.isPermanent() && latestRecord.getActiveUntil().getTime() < new Date().getTime())) {
                  ctx.getSource().sendPlainMessage(
                    new ConfigFormatter(
                      Config.getMessageNode().node("noRecord").getString()
                    )
                      .putKV("player", username)
                      .build()
                  );
                  return Command.SINGLE_SUCCESS;
                }

                latestRecord.deactivateRecord();

                ctx.getSource().sendPlainMessage(
                  new ConfigFormatter(
                    Config.getMessageNode().node("unmuted").getString()
                  )
                    .putKV("player", username)
                    .build()
                );
                return Command.SINGLE_SUCCESS;
              }
            )
        )
        .build()
    );
  }
}
